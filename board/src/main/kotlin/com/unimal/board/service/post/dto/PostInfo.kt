package com.unimal.board.service.post.dto

import com.unimal.board.enums.MapShow
import com.unimal.board.enums.PostShow
import java.time.LocalDateTime

data class PostInfo(
    val boardId: String,
    val email: String,
    val profileImage: String? = null,
    val nickname: String,
    val title: String? = "",
    val content: String,
    val streetName: String,
    val show: PostShow,
    val mapShow: MapShow,
    val createdAt: LocalDateTime,
    val fileInfoList: List<BoardFileInfo?>,
    val likeCount: Long,
    val replyCount: Int,
    val reply: List<Reply>,
    val isOwner: Boolean = false
)
