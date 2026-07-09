package com.unimal.common.enums.report

enum class ReportStatus(
    val description: String
) {
    PENDING("접수"),      // 신고 접수, 미처리 (기본값)
    RESOLVED("처리완료"), // 신고 인정 → 대상 제재
    REJECTED("반려"),     // 신고 기각 → 문제 없음
}
