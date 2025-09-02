package com.unimal.photo.service.s3

import com.unimal.photo.service.s3.dto.FileType
import com.unimal.photo.service.s3.dto.MultipleFiles
import com.unimal.photo.service.s3.dto.UploadFileResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.FileUpload
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.map

@Component
class S3Manager(
    @Value("\${aws.s3.bucket}")
    private val s3Bucket: String,
    private val s3Client: S3Client,
    private val transferManager: S3TransferManager,
) {

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

    fun multiUploadFile(multipleFiles: List<MultipleFiles>): List<UploadFileResult> {
        try {
            // uploadFile 트랜스퍼들을 한 번에 제출 → 내부적으로 병렬 수행
            val transfers: List<FileUpload> = multipleFiles.map { mf ->
                transferManager.uploadFile(
                    UploadFileRequest.builder()
                        .source(mf.path)
                        .putObjectRequest(
                            PutObjectRequest.builder()
                                .bucket(s3Bucket)
                                .key(mf.key)
                                .contentType(Files.probeContentType(mf.path)) // or mf.contentType
                                .build()
                        ).build()
                )
            }

            // 전체 완료 대기
            CompletableFuture.allOf(*transfers.map { it.completionFuture() }.toTypedArray()).join()
            // 결과 수집
            return multipleFiles.map { mf -> UploadFileResult(mf.originalFilename, mf.key) }
        } finally {
            // 임시 파일 정리, _ 는 kotlin 에서 쓰지 않는 변수를 나타내는 특별한 네이밍 규칙
            multipleFiles.forEach { mf -> Files.deleteIfExists(mf.path) }
        }

    }

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

}