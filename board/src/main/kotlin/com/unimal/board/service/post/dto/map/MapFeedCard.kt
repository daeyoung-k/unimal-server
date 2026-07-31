package com.unimal.board.service.post.dto.map

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

/**
 * 피드 바텀카드 1장.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §4
 *
 * **내부 프로퍼티는 camelCase, JSON 키는 snake_case.** 필드마다 `@JsonProperty` 를 붙이는
 * 기존 [MapPostInfo] 방식이 아니라 클래스 레벨 [JsonNaming] 하나로 처리한다 —
 * 필드를 추가할 때 어노테이션을 빼먹어서 키가 camelCase 로 새는 사고가 구조적으로 불가능하다.
 *
 * `isLike` / `isOwner` / `score` 가 **없는 것이 의도**다. 카드는 썸네일·제목·반응수만
 * 보여주고 탭하면 상세(`GET /board/post/{boardId}`)로 가는데 그쪽이 이미 개인화 필드를
 * 내려준다. 응답이 사용자에 의존하지 않아서 **60초 응답 캐시가 성립한다** (스펙 §6).
 * 개인화 필드를 하나라도 되살리면 캐시 키에 email 이 들어가고 히트율이 0이 된다.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MapFeedCard(
    /** Hashids 인코딩된 게시글 ID. raw PK 를 노출하지 않는다. → `board_id` */
    val boardId: String,

    /**
     * 400px JPEG 파생(`board_file.thumb_url`), 없으면 원본으로 폴백.
     * 사진 없는 글도 피드에 포함하므로 **null 가능** — 앱이 플레이스홀더를 그린다.
     */
    val thumbnailUrl: String?,

    /**
     * 원본 이미지. 풀폭 카드에서는 400px 파생이 흐릿하므로 앱이 카드 크기에 따라 고른다.
     * 같은 LATERAL 행에서 함께 읽으므로 추가 쿼리 비용이 없다.
     */
    val imageUrl: String?,

    val title: String?,
    val content: String,
    val streetName: String?,
    val dong: String?,
    val latitude: Double,
    val longitude: Double,

    /**
     * 현재 지도 중심에서의 거리(미터).
     *
     * 반경 제한이 없어 수백 km 떨어진 글일 수 있다. 이걸 숨기면 카드를 탭했을 때 지도가
     * 갑자기 먼 곳으로 튀어서 혼란스럽다. 정직하게 내려주면 탐색이 된다.
     */
    val distanceMeters: Int,

    val nickname: String,
    val profileImage: String?,
    val likeCount: Long,
    val replyCount: Long,
    val createdAt: LocalDateTime,
)
