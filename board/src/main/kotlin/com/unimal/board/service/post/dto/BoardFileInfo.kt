package com.unimal.board.service.post.dto

data class BoardFileInfo(
    val fileId: String,
    val fileUrl: String,
    /** 마커용 400px 썸네일. 백필 전 기존 파일은 null — 앱은 fileUrl 폴백 */
    val thumbUrl: String? = null,
)
