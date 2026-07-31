package com.unimal.board.service.post.dto.map

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.unimal.board.service.post.enums.FeedSectionType

/**
 * 피드 섹션 하나. 세로 스크롤 + 섹션 헤더 구조다 (**탭이 아니다**).
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §4
 *
 * 내부 프로퍼티는 camelCase, JSON 키는 snake_case ([hasMore] → `has_more`).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MapFeedSection(
    val type: FeedSectionType,

    /** 서버가 조립한 헤더 문구. 앱 배포 없이 바꿀 수 있게 하기 위해 응답에 포함한다. */
    val title: String,

    /**
     * `SECTION_LIMIT + 1` 건을 뽑아 초과분 존재로 판정한다.
     * 별도 `COUNT(*)` 를 돌리지 않는다 — 총 개수는 UI에 안 쓰이는데 count 쿼리는 비싸다.
     *
     * 지금은 앱이 화살표(`>`)만 렌더한다. 더보기 페이지네이션은 다음 단계.
     */
    val hasMore: Boolean,

    val items: List<MapFeedCard>,
)
