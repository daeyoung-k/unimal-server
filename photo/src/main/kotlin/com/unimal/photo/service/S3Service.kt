package com.unimal.photo.service

import com.unimal.photo.service.s3.S3Manager
import com.unimal.photo.service.s3.UploadType
import com.unimal.photo.service.s3.dto.UploadFileResult
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3Service(
    private val s3Manager: S3Manager
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
}