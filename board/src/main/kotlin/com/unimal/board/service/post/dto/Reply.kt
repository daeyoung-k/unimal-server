package com.unimal.board.service.post.dto

interface ReplyInterface {
    val replyId: String
    val email: String
    val nickname: String
    val comment: String
}

data class Reply(
    override val replyId: String,
    override val email: String,
    override val nickname: String,
    override val comment: String,
): ReplyInterface

data class PostReply(
    override val replyId: String,
    override val email: String,
    override val nickname: String,
    override val comment: String,
    val reReply: List<Reply>
): ReplyInterface