package com.unimal.board.domain.board.map

import java.time.LocalDateTime

/**
 * 피드 후보 1건 — 리포지토리 내부 전용 표현.
 *
 * 응답 DTO([com.unimal.board.service.post.dto.map.MapFeedCard])와 분리한 이유:
 * - `id` 가 raw Long 이다. Hashids 인코딩은 서비스 조립 단계에서 한다.
 *   중복 제거·정렬을 Long 으로 처리해야 문자열 비교 O(n×m) 를 피할 수 있다.
 * - `thumbUrl` / `fileUrl` 을 각각 들고 있다. 폴백(`thumbUrl ?: fileUrl`)은 조립 단계 책임.
 */
data class MapFeedCandidate(
    val id: Long,
    val title: String?,
    val content: String,
    val streetName: String?,
    val dong: String?,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val createdAt: LocalDateTime,
    val nickname: String?,
    val profileImage: String?,
    val thumbUrl: String?,
    val fileUrl: String?,
    val likeCount: Long,
    val replyCount: Long,
)
