package com.unimal.photo.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3Service(
    @Value("\${aws.s3.bucket}")
    private val s3Bucket: String,
    private val s3Client: S3Client,
) {

    fun uploadFile(file: MultipartFile) {

        val fileReq = PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key("images/${file.originalFilename}")
            .build()

        s3Client.putObject(fileReq, RequestBody.fromInputStream(file.inputStream, file.size))

    }
}