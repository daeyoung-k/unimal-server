package com.unimal.photo.controller

import com.unimal.common.TestDTO
import com.unimal.common.dto.CommonResponse
import com.unimal.photo.service.S3Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@RestController
class PhotoController(
    private val s3Service: S3Service
) {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun upload(
        @RequestBody file: MultipartFile
    ): CommonResponse {
        s3Service.uploadFile(file)
        return CommonResponse()
    }
}