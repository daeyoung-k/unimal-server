package com.unimal.board.service.posts.dto

import java.time.LocalDateTime

data class PostsInfo(
    val id: String,
    val email: String,
    val profileImage: String? = null,
    val nickname: String,
    val title: String? = "",
    val content: String,
    val streetName: String,
    val createdAt: LocalDateTime,
    val imageUrlList: List<String>
)
