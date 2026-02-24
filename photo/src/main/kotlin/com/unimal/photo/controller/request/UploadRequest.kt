package com.unimal.photo.controller.request

import org.springframework.web.multipart.MultipartFile

data class UploadRequest(
    val file: MultipartFile,
    val folder: String? = null
)
