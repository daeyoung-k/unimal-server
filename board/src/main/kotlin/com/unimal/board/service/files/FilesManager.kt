package com.unimal.board.service.files

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.BoardFileRepository
import com.unimal.common.dto.file.UploadFileResult
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.FileException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

@Component
class FilesManager(
    @Value("\${etc.base-url.photo}")
    private val baseUrl: String,
    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,

    private val boardFileRepository: BoardFileRepository,
) {
    private val logger = KotlinLogging.logger {}

    // 단건 `/photo/upload` 호출은 제거했다. board 의 파일 업로드는 1장이든
    // 10장이든 multipleUploadFile 한 경로만 탄다. (프로필 이미지를 올리는
    // user 모듈의 FileService.uploadFileHttp 는 별개다)

    /**
     * photo 서비스에 파일 전부를 한 번에 넘긴다. 응답 순서는 요청 순서와 같다.
     */
    fun multipleUploadFile(
        files: List<MultipartFile>
    ): List<UploadFileResult> {
        val url = "$baseUrl/photo/multiple-upload"

        val restClient = RestClient.create()

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
            // TypeReference 로 원소 타입까지 알려준다.
            //
            // 예전엔 `treeToValue(tree, List::class.java) as List<UploadFileResult>`
            // 였다. raw List 로 읽으면 실제로는 List<LinkedHashMap> 이 만들어지고,
            // 캐스팅은 unchecked 라 컴파일도 통과한다. 원소를 처음 꺼내 쓰는 순간
            // ClassCastException 이 터지는데, 이 메서드를 아무도 호출하지 않던
            // 동안에는 드러나지 않았다.
            return mapper.treeToValue(tree, object : TypeReference<List<UploadFileResult>>() {})

        } catch (e: Exception) {
            // 사용자에게는 고정 문구가 나가지만, photo 가 알려준 실제 원인
            // ("다중 업로드 실패 (1/10): [...]" 등)은 로그에 남겨야 추적이 된다.
            logger.error(e) { "다중 파일 업로드 오류 (${files.size}건)" }
            throw FileException(ErrorCode.MULTIFILE_UPLOAD_ERROR.message)
        }
    }

    /**
     * 게시글 사진을 올리고 board_file 로 저장한다.
     *
     * 예전에는 파일마다 `/photo/upload` 를 순차 호출했다. 10장을 올리면 board →
     * photo HTTP 요청이 10번 줄지어 나가고, 매번 RestClient 를 새로 만들어
     * 커넥션도 재사용하지 못했다. 게다가 그 안에서 썸네일 생성까지 하니 장수가
     * 늘수록 체감이 급격히 나빠졌다.
     *
     * 이제 `/photo/multiple-upload` 한 번으로 넘기고, photo 쪽에서
     * S3TransferManager 가 병렬로 올린다. 1장짜리도 같은 경로를 쓴다 —
     * 경로가 둘이면 한쪽만 고치는 사고가 난다(실제로 썸네일이 그랬다).
     */
    fun uploadFile(
        board: Board,
        files: List<MultipartFile>,
        mainOption: Boolean = false
    ) {
        if (files.isEmpty()) return

        val uploaded = multipleUploadFile(files)

        uploaded.forEachIndexed { index, uploadFileInfo ->
            // 메인파일이 있으면 main 옵션은 모두 false로 설정, 메인파일 정보가 없을때 첫 인덱스만 메인으로 설정한다.
            // photo 가 요청 순서를 그대로 돌려주므로 여기서의 index 는 사용자가
            // 고른 순서와 같다.
            val main = if (mainOption) false else (index == 0)
            val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key
            val thumbUrl = uploadFileInfo.thumbKey?.let { "$fileBaseUrl/$it" }

            boardFileRepository.save(
                BoardFile(
                    board = board,
                    main = main,
                    fileName = uploadFileInfo.originalFilename,
                    fileKey = uploadFileInfo.key,
                    fileUrl = fileUrl,
                    thumbKey = uploadFileInfo.thumbKey,
                    thumbUrl = thumbUrl
                )
            )
        }
    }
}