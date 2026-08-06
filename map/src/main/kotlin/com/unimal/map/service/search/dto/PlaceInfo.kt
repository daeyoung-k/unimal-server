package com.unimal.map.service.search.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 장소 검색 결과 한 건.
 *
 * 필드명은 map 모듈의 기존 DTO(AddressResult)와 같은 camelCase 를 따른다.
 * Redis 에 JSON 으로 직렬화해 캐싱하므로 역직렬화용 기본값을 둔다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PlaceInfo(
    val title: String = "",
    val address: String = "",
    val roadAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
