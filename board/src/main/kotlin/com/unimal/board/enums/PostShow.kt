package com.unimal.board.enums

enum class PostShow(
    val description: String
) {
    PUBLIC("전체 공개"),
    FRIENDS("친구만 공개"),
    PRIVATE("비공개"),
    SAME("지도, 게시글 노출 설정 동일")
}