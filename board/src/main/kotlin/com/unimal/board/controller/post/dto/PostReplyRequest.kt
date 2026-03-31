package com.unimal.board.controller.post.dto

import jakarta.validation.constraints.NotBlank

data class PostReplyRequest(
    @field:NotBlank
    val comment: String,
    val replyId: String? = null
)
