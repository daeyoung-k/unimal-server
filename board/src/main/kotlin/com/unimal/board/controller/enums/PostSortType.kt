package com.unimal.board.controller.enums

enum class PostSortType(
    val description: String
) {
    LATEST("최신순"),
    LIKES("좋아요순"),
    REPLYS("댓글순")
}