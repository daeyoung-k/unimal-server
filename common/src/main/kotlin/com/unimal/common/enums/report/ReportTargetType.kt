package com.unimal.common.enums.report

enum class ReportTargetType(
    val description: String
) {
    POST("게시글 유형"),
    REPLY("댓글 유형"),
    USER("유저 유형")
}