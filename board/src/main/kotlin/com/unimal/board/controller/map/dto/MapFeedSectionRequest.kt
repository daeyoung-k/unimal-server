package com.unimal.board.controller.map.dto

import com.unimal.board.service.post.enums.FeedSectionType

/**
 * 피드 **섹션 1개** 조회 요청 (2026-07-31).
 *
 * 앱의 섹션별 새로고침 버튼이 쓴다. 전체 피드([MapFeedRequest])와 좌표 파라미터는
 * 같고 [type] 만 더 받는다.
 *
 * ## 왜 [type] 을 enum 으로 받나
 *
 * 앱은 **자기가 방금 받은 섹션의 type 을 그대로 되돌려 보낸다.** 즉 서버가 내려준
 * 값만 올라오므로, 모르는 값이 오면 그건 계약 위반이지 정상 입력이 아니다.
 * Spring 이 400 으로 끊어주는 편이 낫다 — String 으로 받아 조용히 null 섹션을
 * 내려주면 앱에서는 "새로고침했더니 섹션이 사라졌다"로 보여 원인을 못 찾는다.
 */
data class MapFeedSectionRequest(
    val latitude: Double,
    val longitude: Double,

    /** 갱신할 섹션. 앱이 응답으로 받았던 `sections[].type` 을 그대로 보낸다. */
    val type: FeedSectionType,

    /** [MapFeedRequest.zoom] 과 같다 — 현재 미사용, 반경 캡 도입 대비. */
    val zoom: Int? = null,

    /** [MapFeedRequest.refresh] 와 같다. 섹션 새로고침은 사실상 항상 true 로 온다. */
    val refresh: Boolean = false,
)
