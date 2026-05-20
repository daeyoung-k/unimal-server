package com.unimal.common.enums.report

enum class ReportStatus(
    val decsription: String
) {
    PENDING("대기중"),
    REVIEWED("처리중"),
    DISMISSED("반려"),
    SUCCESSED("성공")
}