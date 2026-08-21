package com.unimal.photo.service

import com.unimal.common.dto.file.UploadFileResult
import com.unimal.photo.service.s3.S3Manager
import com.unimal.photo.service.s3.ThumbnailManager
import com.unimal.photo.service.s3.UploadType
import com.unimal.photo.service.s3.dto.MultipleFiles
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.collections.map

@Service
class S3Service(
    private val s3Manager: S3Manager,
    private val thumbnailManager: ThumbnailManager,
) {

    private val logger = KotlinLogging.logger {}

    fun uploadFile(
        file: MultipartFile,
        folder: String? = null,
        thumbnail: Boolean = false,
    ): UploadFileResult {
        val originalFilename = file.originalFilename ?: "unnamed"
        val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

        val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
        val fileType = UploadType.from(getType.type)

        val folderPath = if (folder.isNullOrBlank()) "" else "$folder/"
        val key = fileType.path + folderPath + encodedFilename + "." + getType.subType

        // 썸네일 미대상: 원본만 스트림 업로드 (기존 동작 유지)
        if (!thumbnail || !thumbnailManager.isImage(file.contentType)) {
            s3Manager.uploadFile(key, file)
            return UploadFileResult(
                originalFilename = originalFilename,
                key = key
            )
        }

        // 썸네일 대상: 스트림을 두 번 읽어야 하므로 임시 파일 경유
        val tmp = Files.createTempFile("unimal-", ".${getType.subType}")
        try {
            file.inputStream.use { Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING) }
            s3Manager.uploadFile(key, tmp, file.contentType)

            // 썸네일 생성·업로드 실패는 원본 업로드 실패로 전파하지 않는다 (thumbKey null → 앱 폴백)
            val thumbKey = thumbnailManager.createThumbFile(tmp)?.let { thumbTmp ->
                try {
                    val derivedKey = ThumbnailManager.THUMB_PATH + folderPath +
                            encodedFilename + "." + ThumbnailManager.THUMB_EXTENSION
                    s3Manager.uploadFile(derivedKey, thumbTmp, ThumbnailManager.THUMB_CONTENT_TYPE)
                } catch (e: Exception) {
                    logger.warn(e) { "썸네일 업로드 실패 — 원본만 유지: $key" }
                    null
                } finally {
                    Files.deleteIfExists(thumbTmp)
                }
            }

            return UploadFileResult(
                originalFilename = originalFilename,
                key = key,
                thumbKey = thumbKey
            )
        } finally {
            Files.deleteIfExists(tmp)
        }
    }

    /**
     * 멀티 파일 업로드 - S3TransferManager 사용
     * MultipartFile → 임시 파일 생성 → 업로드 → 임시 파일 삭제
     *
     * 왜 tmp(Path) 사용?
     * Transfer Manager의 최적 경로가 “파일(Path)” 기준
     * MultipartFile의 내부는 대개 요청(InputStream) 에 의존. 컨트롤러가 리턴되고 요청이 끝나면 컨테이너가 스트림을 닫거나 청소할 수 있음.
     * Transfer Manager 업로드는 비동기라 메서드 리턴 후에도 전송이 계속될 수 있는데, 스트림이 사라지면 업로드가 깨짐.
     * 그래서 디스크에 안전하게 복사해서, 업로드가 끝날 때까지 데이터가 안정적으로 존재하도록 함.
     *
     * 대용량/다중 파일에서 메모리 폭주 방지
     * 10장 동시 업로드를 모두 메모리에 올려두면 힙을 크게 잡아먹고 GC 압박이 커짐.
     * 임시 파일로 내려두면 메모리 사용량을 일정하게 유지하면서 병렬 업로드 가능.
     */
    fun multiUploadFile(files: List<MultipartFile>): List<UploadFileResult> {
        // 만든 임시 파일 경로를 전부 모아둔다.
        //
        // 정상 흐름의 임시 파일 정리는 S3Manager 의 finally 가 하지만, 그건
        // 리스트가 완성돼 넘어가야 돈다. 5번째 파일에서 createTempFile 이나
        // copy 가 실패하면 앞서 만든 4개(와 그 썸네일)가 디스크에 그대로 남는다.
        // MultipleFiles 가 아니라 Path 를 모으는 이유는, copy 도중 실패하면
        // 그 파일의 tmp 는 만들어졌지만 MultipleFiles 는 아직 없기 때문이다.
        val tmpPaths = mutableListOf<Path>()

        val multipleTmpFiles = try {
            files.map { file ->
                val originalFilename = file.originalFilename ?: "unnamed"
                val encodedFilename = s3Manager.base64EncodeAndUUIDString(originalFilename)

                val getType = s3Manager.getFileType(file.contentType ?: "etc/unknown")
                val fileType = UploadType.from(getType.type)

                val key = fileType.path + encodedFilename + "." + getType.subType

                val tmp = Files.createTempFile("unimal-", ".${getType.subType}")
                    .also(tmpPaths::add)
                file.inputStream.use { Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING) }

                // 마커용 썸네일 파생 — 단일 업로드 경로와 같은 규칙으로 만든다.
                //
                // 예전엔 이 경로에 썸네일이 아예 없었다. 게시글 사진이 단일 경로로만
                // 올라가던 동안에는 드러나지 않았지만, 다중 경로로 전환하면서 그대로
                // 뒀다면 마커가 원본(수 MB)을 받아 지도가 무거워졌을 것이다.
                var thumbKey: String? = null
                var thumbPath: Path? = null
                if (thumbnailManager.isImage(file.contentType)) {
                    thumbnailManager.createThumbFile(tmp)?.let { thumbTmp ->
                        // `+=` 를 쓰면 안 된다. java.nio.file.Path 는 Iterable<Path>
                        // 라서 plusAssign 의 "원소 하나" 와 "컬렉션 전부" 오버로드에
                        // 모두 걸린다. 후자로 붙으면 경로가 아니라 경로의 이름
                        // 조각들이 담긴다. add 는 그런 모호함이 없다.
                        tmpPaths.add(thumbTmp)
                        thumbKey = ThumbnailManager.THUMB_PATH + encodedFilename +
                                "." + ThumbnailManager.THUMB_EXTENSION
                        thumbPath = thumbTmp
                    }
                }

                MultipleFiles(
                    originalFilename = originalFilename,
                    key = key,
                    path = tmp,
                    contentType = file.contentType,
                    thumbKey = thumbKey,
                    thumbPath = thumbPath,
                )
            }
        } catch (e: Exception) {
            tmpPaths.forEach { runCatching { Files.deleteIfExists(it) } }
            throw e
        }

        return s3Manager.multiUploadFile(multipleTmpFiles)
    }

    fun deleteFile(
        keys: List<String>
    ) {
        if (keys.isEmpty()) return

        if (keys.size == 1) {
            s3Manager.deleteFile(keys.first())
        } else {
            s3Manager.multipleDeleteFile(keys)
        }
    }
}