package com.unimal.board.service.post.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Reply(
    val id: String,
    val boardId: String,
    val replyId: String? = null,
    val reReplyYn: Boolean,
    val email: String,
    val nickname: String,
    @JsonProperty("profile_image")
    val profileImage: String? = null,
    val comment: String,
    val createdAt: String,
    val isOwner: Boolean = false,
    val isDel: Boolean = false
)

