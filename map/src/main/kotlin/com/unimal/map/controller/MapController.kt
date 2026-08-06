package com.unimal.map.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.controller.request.PlaceSearchRequest
import com.unimal.map.service.MapService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RestController

@RestController
class MapController(
    private val mapService: MapService,
) {

    @GetMapping("/reverse-geocoding")
    fun reverseGeocoding(
        @ModelAttribute @Valid latLngRequest: LatLngRequest
    ): CommonResponse {
        return CommonResponse(data = mapService.reverseGeocoding(latLngRequest))
    }

    /**
     * 장소 검색 프록시. 게이트웨이의 /map/~ 은 인증 필터가 없어 비로그인도 호출 가능하다.
     * 검색어가 짧거나 외부 API 가 실패해도 400/500 대신 빈 목록을 준다.
     * 검색창은 타이핑 중 계속 호출되는 자리라, 에러를 띄우는 쪽이 사용자 경험을 더 해친다.
     */
    @GetMapping("/search/place")
    fun searchPlace(
        @ModelAttribute placeSearchRequest: PlaceSearchRequest
    ): CommonResponse {
        return CommonResponse(data = mapService.searchPlace(placeSearchRequest))
    }
}
