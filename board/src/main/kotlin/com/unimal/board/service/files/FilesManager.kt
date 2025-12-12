package com.unimal.board.service.files

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.BoardFileRepository
import com.unimal.board.service.files.dto.UploadFileResult
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.FileException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

@Component
class FilesManager(
    @Value("\${etc.base-url}")
    private val baseUrl: String,
    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,

    private val boardFileRepository: BoardFileRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun uploadFileHttp(
        file: MultipartFile
    ): UploadFileResult {
        val url = "$baseUrl/photo/upload"

        val restClient = RestClient.create()

        val multipartBody = MultipartBodyBuilder().apply {
            part("file", file.resource)
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

    fun multipleUploadFile(
        files: List<MultipartFile>
    ): List<UploadFileResult> {
        val url = "$baseUrl/photo/multiple-upload"

        val restClient = RestClient.create()

        val formBody = LinkedMultiValueMap<String, Any>()
        formBody.add("files", files)

        val multipartBody = MultipartBodyBuilder().apply {
            files.forEach { file -> part("files", file.resource) }
        }.build()

        try {
            val response = restClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipartBody)
                .retrieve()
                .body(String::class.java)

            val mapper = jacksonObjectMapper()

            val tree = mapper.readTree(response)["data"]
            return mapper.treeToValue(tree, List::class.java) as List<UploadFileResult>

        } catch (e: Exception) {
            logger.error(e) { "다중 파일 업로드 오류: $e" }
            throw FileException(ErrorCode.MULTIFILE_UPLOAD_ERROR.message)
        }
    }

    fun uploadFile(
        board: Board,
        files: List<MultipartFile>,
        mainOption: Boolean = false
    ) {
        files.forEachIndexed { index, file ->
            // 메인파일이 있으면 main 옵션은 모두 false로 설정, 메인파일 정보가 없을때 첫 인덱스만 메인으로 설정한다.
            val main = if (mainOption) false else (index == 0)
            val uploadFileInfo = uploadFileHttp(file)
            val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key

            boardFileRepository.save(
                BoardFile(
                    board = board,
                    main = main,
                    fileName = uploadFileInfo.originalFilename,
                    fileKey = uploadFileInfo.key,
                    fileUrl = fileUrl
                )
            )
        }
    }
}