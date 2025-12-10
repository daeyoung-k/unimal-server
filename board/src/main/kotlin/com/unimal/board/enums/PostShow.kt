package com.unimal.board.enums

enum class PostShow(
    val description: String
) {
    PUBLIC("전체 공개"),
    FRIENDS("친구만 공개"),
    PRIVATE("비공개"),
}