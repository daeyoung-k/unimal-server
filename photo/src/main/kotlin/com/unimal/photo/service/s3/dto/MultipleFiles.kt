package com.unimal.photo.service.s3.dto

import java.nio.file.Path

/**
 * 다중 업로드 한 건의 준비 결과.
 *
 * MultipartFile 을 그대로 들고 다니지 않고 임시 파일 경로로 바꿔두는 이유는
 * S3Service.multiUploadFile 주석 참고 (비동기 전송이 요청 종료 후에도 이어진다).
 */
data class MultipleFiles(
    val originalFilename: String,
    /** 원본 S3 키. */
    val key: String,
    /** 원본 임시 파일. */
    val path: Path,
    /**
     * 원본 Content-Type.
     *
     * 예전엔 S3Manager 가 `Files.probeContentType(path)` 로 추론했는데, 임시
     * 파일의 확장자만 보므로 원본이 알려준 값보다 부정확하다. 업로더가 아는
     * 값을 그대로 들고 내려온다.
     */
    val contentType: String?,
    /** 마커용 썸네일 S3 키. 썸네일 대상이 아니면 null. */
    val thumbKey: String? = null,
    /** 썸네일 임시 파일. 생성 실패 시 null. */
    val thumbPath: Path? = null,
)
