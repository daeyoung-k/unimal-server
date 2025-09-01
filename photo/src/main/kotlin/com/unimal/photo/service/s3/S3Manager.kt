package com.unimal.photo.service.s3

import com.unimal.photo.service.s3.dto.FileType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Component
class S3Manager(
    @Value("\${aws.s3.bucket}")
    private val s3Bucket: String,
    private val s3Client: S3Client
) {

    fun fileUpload(
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