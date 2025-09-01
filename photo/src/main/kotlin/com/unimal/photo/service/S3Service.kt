package com.unimal.photo.service

import com.unimal.photo.service.s3.S3Manager
import com.unimal.photo.service.s3.UploadType
import com.unimal.photo.service.s3.dto.UploadFileResult
import org.springframework.cglib.core.CollectionUtils.bucket
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.FileUpload
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.CompletableFuture
import kotlin.collections.map

@Service
class S3Service(
    private val s3Manager: S3Manager,
    private val transferManager: S3TransferManager,
) {

    fun uploadFile(file: MultipartFile): UploadFileResult {
        val originalFilename = file.originalFilename ?: "unnamed"
        val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

        val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
        val fileType = UploadType.from(getType.type)

        val key = fileType.path + encodedFilename + "." + getType.subType
        s3Manager.fileUpload(key, file)

        return UploadFileResult(
            originalFilename = originalFilename,
            key = key
        )
    }

    fun multiUploadFile(files: List<MultipartFile>) {

        val tempFiles = files.map { file ->
            val originalFilename = file.originalFilename ?: "unnamed"
            val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

            val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
            val fileType = UploadType.from(getType.type)

            val key = fileType.path + encodedFilename + "." + getType.subType
            val tmp = Files.createTempFile("unimal-", ".${getType.subType }")
            file.inputStream.use { Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING) }
            Triple(file.originalFilename ?: "unnamed", key, tmp)
        }
        println(tempFiles)

        try {
            // uploadFile 트랜스퍼들을 한 번에 제출 → 내부적으로 병렬 수행
            val transfers: List<FileUpload> = tempFiles.map { (original, key, path) ->
                transferManager.uploadFile(
                    UploadFileRequest.builder()
                        .source(path)
                        .putObjectRequest(
                            PutObjectRequest.builder()
                                .bucket("unimal-bucket")
                                .key(key)
                                .contentType(Files.probeContentType(path)) // or mf.contentType
                                .build()
                        ).build()
                )
            }

            // 전체 완료 대기
            CompletableFuture.allOf(*transfers.map { it.completionFuture() }.toTypedArray()).join()
            // 결과 수집
//            return tempFiles.map { (original, key, _) -> UploadResult(original, key) }
        } finally {
            // 임시 파일 정리
            tempFiles.forEach { (_, _, p) -> Files.deleteIfExists(p) }
        }

    }
}