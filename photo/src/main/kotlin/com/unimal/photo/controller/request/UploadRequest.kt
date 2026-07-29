package com.unimal.photo.controller.request

import org.springframework.web.multipart.MultipartFile

data class UploadRequest(
    val file: MultipartFile,
    val folder: String? = null,
    /** true 이고 이미지 파일이면 마커용 400px JPEG 썸네일을 함께 생성한다 */
    val thumbnail: Boolean = false,
)
