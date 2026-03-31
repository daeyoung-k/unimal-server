package com.unimal.board.enums

enum class NoticeType(
    val description: String
) {
    NOTICE("일반 공지"),
    EVENT("이벤트"),
    UPDATE("업데이트"),
    MAINTENANCE("점검"),
    POLICY("정책"),
    GUIDE("이용 안내"),
    CAMPAIGN("캠페인")
}