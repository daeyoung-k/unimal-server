package com.unimal.board.controller.notice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NoticeResponse(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    @field:JsonProperty("created_at")
    val createdAt: String,
)
