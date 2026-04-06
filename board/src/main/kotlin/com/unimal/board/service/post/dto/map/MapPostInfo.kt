package com.unimal.board.service.post.dto.map

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class MapPostInfo(
    val id: String,
    val title: String?,
    val content: String,
    @JsonProperty("street_name")
    val streetName: String?,
    val latitude: Double,
    val longitude: Double,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime,
    @JsonProperty("file_url")
    val fileUrl: String?,
    @JsonProperty("like_count")
    val likeCount: Long,
    @JsonProperty("reply_count")
    val replyCount: Long,
    val score: Double,
)
