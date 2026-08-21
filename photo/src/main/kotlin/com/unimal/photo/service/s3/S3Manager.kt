package com.unimal.photo.service.s3

import com.unimal.common.dto.file.UploadFileResult
import com.unimal.photo.service.s3.dto.FileType
import com.unimal.photo.service.s3.dto.MultipleFiles
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.FileUpload
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import java.nio.file.Files
import java.util.*
import kotlin.collections.map

@Component
class S3Manager(
    @Value("\${aws.s3.bucket}")
    private val s3Bucket: String,
    private val s3Client: S3Client,
    private val transferManager: S3TransferManager,
) {

    private val logger = KotlinLogging.logger {}

    fun uploadFile(
        key: String,
        file: MultipartFile
    ): String {
        val fileReq = PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .contentType(file.contentType)
            .build()
        s3Client.putObject(fileReq, RequestBody.fromInputStream(file.inputStream, file.size))
        return key
    }

    fun uploadFile(
        key: String,
        path: java.nio.file.Path,
        contentType: String?,
    ): String {
        val fileReq = PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .contentType(contentType)
            .build()
        s3Client.putObject(fileReq, RequestBody.fromFile(path))
        return key
    }

    /**
     * 원본 + 썸네일을 한 번에 제출해 병렬 업로드한다.
     *
     * 실패 정책은 단일 업로드 경로와 맞춘다.
     * - **원본**: 하나라도 실패하면 전체 실패. 이때 이미 올라간 오브젝트를 지워
     *   S3 에 고아 파일이 남지 않게 한다. 예전에는 `allOf(...).join()` 하나로
     *   묶어 던지기만 해서, 10장 중 9장이 올라간 뒤 1장이 실패하면 그 9장이
     *   아무도 참조하지 않는 채 버킷에 영영 남았다.
     * - **썸네일**: 개별 실패를 허용하고 thumbKey 를 null 로 돌려준다.
     *   (앱이 원본 이미지로 폴백한다)
     */
    fun multiUploadFile(multipleFiles: List<MultipleFiles>): List<UploadFileResult> {
        // 제출한 전송을 전부 여기 모은다. finally 에서 "아직 도는 전송이 없는지"
        // 확인하는 용도다 — 전송 중인 임시 파일을 지우면 업로드가 조용히 깨진다.
        val submitted = mutableListOf<FileUpload>()
        try {
            // 원본·썸네일을 모두 먼저 제출해 동시에 돌린다. 결과 판정은 그다음.
            val originUploads: List<Pair<MultipleFiles, FileUpload>> =
                multipleFiles.map { mf ->
                    mf to submit(mf.key, mf.path, mf.contentType).also(submitted::add)
                }

            val thumbUploads: List<Pair<MultipleFiles, FileUpload>> = multipleFiles
                .filter { it.thumbKey != null && it.thumbPath != null }
                .map { mf ->
                    mf to submit(mf.thumbKey!!, mf.thumbPath!!, ThumbnailManager.THUMB_CONTENT_TYPE)
                        .also(submitted::add)
                }

            // 썸네일 먼저 걷는다. 여기서 실패해도 흐름을 막지 않는다.
            val uploadedThumbKeys = mutableSetOf<String>()
            thumbUploads.forEach { (mf, upload) ->
                try {
                    upload.completionFuture().join()
                    uploadedThumbKeys += mf.thumbKey!!
                } catch (e: Exception) {
                    logger.warn(e) { "썸네일 업로드 실패 — 원본만 유지: ${mf.thumbKey}" }
                }
            }

            // 원본은 전부 성공해야 한다.
            val failed = mutableListOf<MultipleFiles>()
            val uploaded = mutableListOf<MultipleFiles>()
            originUploads.forEach { (mf, upload) ->
                try {
                    upload.completionFuture().join()
                    uploaded += mf
                } catch (e: Exception) {
                    logger.error(e) { "원본 업로드 실패: ${mf.key}" }
                    failed += mf
                }
            }

            if (failed.isNotEmpty()) {
                // 성공분을 되돌린다. 이 정리 자체가 실패해도 원래 예외를 덮지 않는다.
                //
                // 썸네일은 uploadedThumbKeys 전부를 지운다. 원본이 실패한 항목의
                // 썸네일이 먼저 올라가 있을 수 있는데, 성공한 원본(uploaded)에서만
                // 뽑으면 그 썸네일이 아무도 참조하지 않는 채 버킷에 남는다.
                val orphans = uploaded.map { it.key } + uploadedThumbKeys
                runCatching { multipleDeleteFile(orphans) }
                    .onFailure { logger.error(it) { "실패한 업로드의 정리 실패 — 고아 파일 확인 필요: $orphans" } }

                throw IllegalStateException(
                    "다중 업로드 실패 (${failed.size}/${multipleFiles.size}): ${failed.map { it.originalFilename }}"
                )
            }

            // 입력 순서를 그대로 유지한다. 호출부가 첫 번째를 대표 이미지로 쓰기 때문에
            // 순서가 섞이면 엉뚱한 사진이 마커에 걸린다.
            return multipleFiles.map { mf ->
                UploadFileResult(
                    originalFilename = mf.originalFilename,
                    key = mf.key,
                    thumbKey = mf.thumbKey?.takeIf(uploadedThumbKeys::contains),
                )
            }
        } finally {
            // 임시 파일을 지우기 전에 아직 도는 전송이 없는지 확인한다.
            // 정상 흐름에서는 위에서 이미 전부 join 했으므로 즉시 통과하고,
            // 제출 도중 예외가 난 경우에만 실제로 기다린다. 이 확인이 없으면
            // 앞서 제출된 전송이 읽는 중인 파일을 지워 업로드가 깨진다.
            submitted.forEach { runCatching { it.completionFuture().join() } }
            multipleFiles.forEach { mf ->
                Files.deleteIfExists(mf.path)
                mf.thumbPath?.let { Files.deleteIfExists(it) }
            }
        }
    }

    private fun submit(key: String, path: java.nio.file.Path, contentType: String?): FileUpload =
        transferManager.uploadFile(
            UploadFileRequest.builder()
                .source(path)
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(key)
                        .contentType(contentType)
                        .build()
                ).build()
        )

    fun base64EncodeAndUUIDString(value: String): String {
        val encodeResult = Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))
        val uuidString = UUID.randomUUID().toString().replace("-", "")
        return "$encodeResult-$uuidString"
    }

    fun getFileType(type: String): FileType {
        val sp = type.split("/")
        return FileType(
            type = sp.first().lowercase(),
            subType = sp.last().lowercase()
        )
    }

    fun deleteFile(
        key: String
    ) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    fun multipleDeleteFile(
        keys: List<String>
    ) {
        if (keys.isEmpty()) return

        val objects = keys.map {
            ObjectIdentifier.builder()
                .key(it)
                .build()
        }

        val request = DeleteObjectsRequest.builder()
            .bucket(s3Bucket)
            .delete(
                Delete.builder()
                    .objects(objects)
                    .build()
            )
            .build()

        s3Client.deleteObjects(request)
    }

}