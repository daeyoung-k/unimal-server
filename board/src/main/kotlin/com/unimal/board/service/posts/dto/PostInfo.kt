package com.unimal.board.service.posts.dto

import java.time.LocalDateTime

data class PostInfo(
    val boardId: String,
    val email: String,
    val profileImage: String? = null,
    val nickname: String,
    val title: String? = "",
    val content: String,
    val streetName: String,
    val public: Boolean? = false,
    val createdAt: LocalDateTime,
    val imageUrlList: List<String>,
    val likeCount: Int,
    val replyCount: Int,
    val reply: List<PostReply>
)
