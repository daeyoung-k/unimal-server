package com.unimal.board.controller.request.post

import jakarta.validation.constraints.NotBlank

data class PostReplyRequest(
    @field:NotBlank
    val comment: String,
    val replyId: String? = null
)
