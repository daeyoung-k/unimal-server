package com.unimal.common.enums

enum class AppPushType(
    private val description: String,
    val title: String,
) {
    LIKE("게시글 좋아요", "스토맵 ❤️"),
    REPLY("게시글 댓글", "스토맵 💬"),
    NOTICE("공지사항", "스토맵 공지사항 📢"),
    EVENT("각종 이벤트", "스토맵 이벤트 🎉")
}