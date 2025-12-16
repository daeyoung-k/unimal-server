package com.unimal.board.service.post.dto

data class Reply(
    val id: String,
    val boardId: String,
    val replyId: String? = null,
    val reReplyYn: Boolean,
    val email: String,
    val nickname: String,
    val comment: String,
    val createdAt: String,
)

