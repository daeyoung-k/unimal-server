package com.unimal.photo.service.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
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
        val originalFilename = file.originalFilename ?: "unnamed"
        val key = path + originalFilename
        println("Uploading file with key: $key")
        
        val fileReq = PutObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .contentType(file.contentType)
            .build()
        s3Client.putObject(fileReq, RequestBody.fromInputStream(file.inputStream, file.size))
        println("Successfully uploaded file with key: $key")
        return key
    }

    fun getPresignedUrl(
        key: String
    ): String {
        println("Requesting presigned URL for key: $key")
        
        // 키 존재 여부 확인
        try {
            val headObject = HeadObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .build()
            s3Client.headObject(headObject)
            println("Object exists for key: $key")
        } catch (e: NoSuchKeyException) {
            println("Object does not exist for key: $key")
            throw IllegalArgumentException("File not found with key: $key", e)
        }

        val getObject = GetObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .build()

        val presignedObject = GetObjectPresignRequest.builder()
            .signatureDuration(ofMinutes(3))
            .getObjectRequest(getObject)
            .build()

        val result = s3Presigner.presignGetObject(presignedObject)
        return result.url().toString()
    }

}