package com.unimal.board.controller.map

import com.unimal.board.controller.map.dto.LocationPostRequest
import com.unimal.board.controller.map.dto.MapFeedRequest
import com.unimal.board.controller.map.dto.MapFeedSectionRequest
import com.unimal.board.service.post.MapFeedService
import com.unimal.board.service.post.MapPostService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/map")
class MapController(
    private val mapPostService: MapPostService,
    private val mapFeedService: MapFeedService,
) {

    @GetMapping("/location/post")
    fun locationPostList(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute @Valid locationPostRequest: LocationPostRequest
    ): CommonResponse {
        return CommonResponse(data = mapPostService.getLocationPosts(userInfo, locationPostRequest))
    }

    @GetMapping("/post")
    fun postList(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute @Valid locationPostRequest: LocationPostRequest
    ): CommonResponse {
        return CommonResponse(data = mapPostService.mapPosts(userInfo, locationPostRequest))
    }

    /**
     * 지도 바텀카드 피드. 게이트웨이에서 `OptionalAccessTokenFilter` 를 거치므로
     * 비로그인도 200 을 받는다 (온보딩에서 지도를 먼저 보여주는 흐름을 막지 않기 위해).
     *
     * **`userInfo` 를 받지 않는 것이 의도**다. 응답에 개인화 필드가 없어야 60초 응답
     * 캐시가 성립한다 — 사용자별 값이 하나라도 섞이면 캐시 키에 email 이 들어가고
     * 히트율이 0이 된다. 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §4
     */
    @GetMapping("/feed")
    fun mapFeed(
        @ModelAttribute @Valid mapFeedRequest: MapFeedRequest
    ): CommonResponse {
        return CommonResponse(data = mapFeedService.getFeed(mapFeedRequest))
    }

    /**
     * 피드 섹션 1개만 조회 (2026-07-31). 앱의 섹션별 새로고침 버튼이 쓴다.
     *
     * `/feed` 와 마찬가지로 인증을 받지 않는다 — 같은 캐시를 공유하려면 응답이
     * 사용자에 의존하지 않아야 한다.
     *
     * **`data` 가 null 일 수 있다.** 적응형 섹션 구조라 요청한 섹션이 이번 계산에서
     * 만들어지지 않을 수 있고, 그건 오류가 아니라 "지금은 그 섹션이 없다"는 정상
     * 응답이다 ([MapFeedService.getSection] 주석 참고). 앱은 이때 해당 섹션만
     * 화면에서 지운다.
     *
     * 경로가 `/feed/section` 이라 `/feed` 와 겹치지 않는다 — Spring 은 더 구체적인
     * 패턴을 먼저 매칭한다.
     */
    @GetMapping("/feed/section")
    fun mapFeedSection(
        @ModelAttribute @Valid mapFeedSectionRequest: MapFeedSectionRequest
    ): CommonResponse {
        return CommonResponse(data = mapFeedService.getSection(mapFeedSectionRequest))
    }
}
