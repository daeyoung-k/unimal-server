package com.unimal.board.service.post.dto

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
    val latitude: Double,
    val longitude: Double,
    val show: PostShow,
    val createdAt: LocalDateTime,
    val fileInfoList: List<BoardFileInfo?>,
    val likeCount: Long,
    val replyCount: Long,
    val reply: List<Reply>,
    val isLike: Boolean = false,
    val isOwner: Boolean = false,
    /**
     * 공유 링크. **null 이면 공유할 수 없는 글이므로 앱은 버튼을 숨긴다.**
     *
     * 앱이 도메인을 조립하지 않도록 서버가 완성된 URL 을 내려준다 —
     * 앱은 배포 후 못 고치기 때문이다. 자세한 근거는 `ShareUrlFactory` KDoc.
     *
     * 목록 응답에서는 채우지 않는다(null). 목록에 공유 버튼이 없기도 하고,
     * 항목마다 URL 을 만들어 내려보내면 응답만 커진다.
     */
    val shareUrl: String? = null,
)
