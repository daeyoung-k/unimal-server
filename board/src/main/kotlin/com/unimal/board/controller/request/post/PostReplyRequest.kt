package com.unimal.board.controller.request.post

import jakarta.validation.constraints.NotBlank

data class PostReplyRequest(
    @field:NotBlank
    val content: String
)
