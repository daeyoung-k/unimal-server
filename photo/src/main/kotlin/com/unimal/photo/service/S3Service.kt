package com.unimal.photo.service

import com.unimal.photo.service.s3.S3Manager
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3Service(
    private val s3Manager: S3Manager
) {

    fun uploadFile(file: MultipartFile): String {
        return s3Manager.imageUpload(file)
    }

    fun getFileUrl(key: String): String {
        return s3Manager.getPresignedUrl(key)
    }
}