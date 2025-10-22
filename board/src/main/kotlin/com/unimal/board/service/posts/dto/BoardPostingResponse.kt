package com.unimal.board.service.posts.dto

data class BoardPostingResponse(
    val boardId: String,
    val title: String,
    val content: String,
    val public: Boolean
)
