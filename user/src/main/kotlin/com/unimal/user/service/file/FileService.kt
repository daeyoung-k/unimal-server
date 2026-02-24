package com.unimal.user.service.file

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.common.dto.file.UploadFileResult
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.FileException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

@Service
class FileService(
    @Value("\${etc.base-url.photo}")
    private val baseUrl: String,
) {

    private val logger = KotlinLogging.logger {}

    fun uploadFileHttp(
        file: MultipartFile,
        folder: String? = null
    ): UploadFileResult {
        val url = "$baseUrl/photo/upload"

        val restClient = RestClient.create()

        val multipartBody = MultipartBodyBuilder().apply {
            part("file", file.resource)
            if (!folder.isNullOrBlank()) part("folder", folder)
        }.build()

        try {
            val response = restClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body (multipartBody)
                .retrieve()
                .body(String::class.java)

            val mapper = jacksonObjectMapper()

            val tree = mapper.readTree(response)["data"]
            return mapper.treeToValue(tree, UploadFileResult::class.java)

        } catch (e: Exception) {
            logger.error(e) { "파일 업로드 오류: $e" }
            throw FileException(ErrorCode.FILE_UPLOAD_ERROR.message)
        }
    }

    fun deleteFileHttp(
        keys: List<String>
    ) {
        val url = "$baseUrl/photo/delete"

        val restClient = RestClient.create()

        val body = mapOf("keys" to keys)

        try {
            restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body (body)
                .retrieve()
                .body(String::class.java)

        } catch (e: Exception) {
            logger.error(e) { "파일 삭제 오류: $e" }
            throw FileException(ErrorCode.FILE_ERROR.message)
        }
    }
}