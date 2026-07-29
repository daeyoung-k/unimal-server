package com.unimal.board.service.post.enums

/**
 * 지도 바텀카드 피드 섹션 종류.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §2
 *
 * 지금은 [NEAR] 하나뿐이다. 값이 하나인데 enum 을 두는 건 의도적이다 —
 * 밀도가 오르면 `LATEST`(최신순), `HOT`(시간감쇠 인기순)을 추가하기로 이미 정해져 있다.
 * 기준은 공개 글 1,200건 정도 (섹션당 20건 × 섹션 수 × 2배).
 *
 * 헤더 문구를 서버가 내려주는 이유: 문구를 바꿀 때 앱 스토어 심사를 다시 받지 않아도 된다.
 */
enum class FeedSectionType(val title: String) {
    /** 현재 위치에서 가까운 순. 반경 제한 없음 — 밀도가 낮아도 피드가 비지 않는다. */
    NEAR("지금 여기 이야기"),
}
