package com.unimal.board.controller.map.dto

/**
 * 지도 바텀카드 피드 조회 요청.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §3
 *
 * 마커 조회([LocationPostRequest])와 달리 **반경을 쓰지 않는다.** 거리는 필터가 아니라
 * 정렬 기준이다 (PostGIS KNN). 그래서 `siDo`/`guGun`/`dong` 도 받지 않는다.
 */
data class MapFeedRequest(
    val latitude: Double,
    val longitude: Double,

    /**
     * **현재 미사용.** 쿼리에 전달하지 않으므로 값을 바꿔도 응답이 변하지 않는다.
     *
     * 밀도가 올라 `NEAR` 섹션에 반경 캡을 씌우게 될 때를 대비해 앱이 v1 부터 보내게 해둔다.
     * 그때 서버만 배포하면 되고 앱 재배포가 필요 없다.
     */
    val zoom: Int? = null,
)
