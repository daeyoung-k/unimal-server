package com.unimal.photo.service.s3.dto

data class UploadFileResult(
    val originalFilename: String,
    val key: String,
)
