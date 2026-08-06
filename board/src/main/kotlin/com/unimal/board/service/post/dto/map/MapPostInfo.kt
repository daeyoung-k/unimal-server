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
    @JsonProperty("file_url")
    val fileUrl: String? = "",
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
    /**
     * 공유 링크. 앱이 URL 을 조립하지 않도록 서버가 완성해서 내려준다
     * (`ShareUrlFactory` KDoc 참고 — 앱은 배포하면 못 고친다).
     *
     * **지도 바텀카드가 공유의 주 진입점이다.** 지도 흐름에서는 게시글 상세 화면으로
     * 가는 경로가 없어서(`map_bottom_card.dart` 의 `showDetailButton: false`),
     * 여기에 URL 이 없으면 대부분의 사용자는 공유 버튼을 볼 일이 없다.
     *
     * 마커 쿼리가 `show = 'PUBLIC'` 만 뽑으므로 여기서는 항상 값이 있다.
     * nullable 인 것은 인코딩 전 단계(`findLocationPosts`)의 중간 상태 때문이다 —
     * `MapPostService.mapPosts` 가 hashid 를 채울 때 같이 채운다.
     */
    @JsonProperty("share_url")
    val shareUrl: String? = null,
)
