package com.unimal.board.enums

enum class MapShow(
    val description: String
) {
    SAME("게시글 설정과 동일 옵션"),
    PUBLIC("전체 공개"),
    FRIENDS("친구만 공개"),
    PRIVATE("비공개"),
}