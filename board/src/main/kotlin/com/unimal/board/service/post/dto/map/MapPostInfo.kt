package com.unimal.board.service.post.dto.map

import com.fasterxml.jackson.annotation.JsonProperty
import com.unimal.board.service.post.dto.BoardFileInfo
import java.time.LocalDateTime

data class MapPostInfo(
    val id: String,
    val nickname: String,
    @JsonProperty("profile_image")
    val profileImage: String?,
    val title: String?,
    val content: String,
    @JsonProperty("street_name")
    val streetName: String?,
    val latitude: Double,
    val longitude: Double,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime,
    @JsonProperty("file_info_list")
    val fileInfoList: List<BoardFileInfo>? = emptyList(),
    @JsonProperty("like_count")
    val likeCount: Long,
    @JsonProperty("reply_count")
    val replyCount: Long,
    val score: Double,
    @JsonProperty("is_owner")
    val isOwner: Boolean = false,
    @JsonProperty("is_like")
    val isLike: Boolean = false,
)
