package com.unimal.photo.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.photo.service.S3Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class PhotoController(
    private val s3Service: S3Service
) {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun upload(
        @RequestBody file: MultipartFile
    ): CommonResponse {
        return CommonResponse(data = s3Service.uploadFile(file))
    }

    @GetMapping("/file-url")
    fun getFileUrl(
        @RequestParam(value = "key", required = true) key: String,
    ): CommonResponse {
        return CommonResponse(data = s3Service.getFileUrl(key))
    }
}