package com.unimal.common.dto.file

data class UploadFileResult(
    val originalFilename: String,
    val key: String,
    /** 마커용 썸네일 파생 키. 썸네일 미생성(비이미지, 미요청, 생성 실패) 시 null */
    val thumbKey: String? = null,
)
