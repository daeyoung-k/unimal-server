package com.unimal.photo.service.s3

import io.github.oshai.kotlinlogging.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

/**
 * 마커용 썸네일 파생 생성기.
 *
 * - 긴 변 [THUMB_SIZE]px, 종횡비 유지 (400 근거: 앱 createMarkerImage 가 중앙 정사각을
 *   크롭해 200px 원에 그리므로, 4:3 사진도 중앙 정사각 300px ≥ 200px 로 확대 흐림이 없다.
 *   unimal-flutter docs/specs/2026-07-29-마커-썸네일-파생.md 참고)
 * - 출력은 입력 포맷과 무관하게 JPEG 로 통일 (png 사진도 수백 KB → 수십 KB)
 * - EXIF orientation 은 Thumbnailator 가 자동 반영
 * - 생성 실패는 업로드 실패로 전파하지 않는다 (thumbKey null → 앱이 원본으로 폴백)
 */
@Component
class ThumbnailManager {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val THUMB_SIZE = 400
        const val THUMB_QUALITY = 0.85
        const val THUMB_PATH = "thumbs/"
        const val THUMB_EXTENSION = "jpg"
        const val THUMB_CONTENT_TYPE = "image/jpeg"
    }

    fun isImage(contentType: String?): Boolean {
        return contentType?.startsWith("image/") == true
    }

    /**
     * [source] 이미지에서 썸네일 임시 파일을 생성한다. 실패 시 null.
     * 호출자가 사용 후 임시 파일을 삭제해야 한다.
     */
    fun createThumbFile(source: Path): Path? {
        val tmp = Files.createTempFile("unimal-thumb-", ".$THUMB_EXTENSION")
        return try {
            Thumbnails.of(source.toFile())
                .size(THUMB_SIZE, THUMB_SIZE)
                .imageType(BufferedImage.TYPE_INT_RGB) // 알파 채널 제거 (JPEG 비호환)
                .outputFormat(THUMB_EXTENSION)
                .outputQuality(THUMB_QUALITY)
                .toFile(tmp.toFile())
            tmp
        } catch (e: Exception) {
            logger.warn(e) { "썸네일 생성 실패 — 원본만 업로드: $source" }
            Files.deleteIfExists(tmp)
            null
        }
    }
}
