package com.unimal.photo.service.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration.*

@Component
class S3Manager(
    @Value("\${aws.s3.bucket}")
    private val s3Bucket: String,
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {

    fun imageUpload(
        file: MultipartFile
    ): String {
        val path = "images/"
        val key = path + file.originalFilename
        val fileReq = PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .build()
        s3Client.putObject(fileReq, RequestBody.fromInputStream(file.inputStream, file.size))
        return key
    }

    fun getPresignedUrl(
        key: String
    ): String {
        val getObject = GetObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .build()

        val presignedObject = GetObjectPresignRequest.builder()
            .signatureDuration(ofMinutes(3))
            .getObjectRequest(getObject)
            .build()

        return s3Presigner.presignGetObject(presignedObject).url().toString()
    }

}