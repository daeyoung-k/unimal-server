package com.unimal.board.service.post.dto.map

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * 지도 바텀카드 피드 응답.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §4
 *
 * **섹션이 1개인데 배열로 감싸는 건 의도적이다.** 이 구조 덕에
 *
 * - 밀도가 오르면 `LATEST`/`HOT` 을 **서버에서만** 추가할 수 있다 (앱 재배포 불필요)
 * - 나중에 `AD`/`NOTICE` 섹션을 순서 조정만으로 끼워넣을 수 있다
 *
 * 단, **앱이 "모르는 `type` 은 무시" 로직을 v1 부터 넣어야 한다.** 안 넣으면 나중에
 * 섹션을 추가할 때 구버전 앱이 깨진다. 서버가 강제할 수 없는 앱 쪽 요구사항이다.
 *
 * 글이 아예 없으면 [sections] 가 빈 배열이다 (200, 500 아님).
 *
 * 내부 프로퍼티는 camelCase, JSON 키는 snake_case. [sections] 는 단일 단어라 표기가 같지만,
 * 나중에 필드가 추가될 때 일관되게 적용되도록 클래스에 붙여둔다.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MapFeedResponse(
    val sections: List<MapFeedSection>,
)
