package com.unimal.board.service.post.dto

data class Reply(
    val boardId: String,
    val replyId: String,
    val email: String,
    val nickname: String,
    val comment: String,
    val createdAt: String,
    val reReply: List<ReReply>
)

data class ReReply(
    val boardId: String,
    val replyId: String,
    val reReplyId: String,
    val email: String,
    val nickname: String,
    val comment: String,
    val createdAt: String,
)

data class ReplyResponse(
    val boardId: String,
    val replyId: String,
    val email: String,
    val nickname: String,
    val comment: String,
    val createdAt: String,
)