package com.unimal.map.service.search

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.map.service.search.dto.PlaceInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

/**
 * 네이버 장소 검색 프록시.
 *
 * 이전에는 Flutter 앱이 openapi.naver.com / maps.apigw.ntruss.com 을 직접 호출했다.
 * 그 구조는 클라이언트 시크릿을 .env 로 앱 에셋에 동봉해야 해서 APK/IPA 를 풀면
 * 평문으로 노출됐다. 특히 NCP 키는 종량 과금이라 유출이 곧 비용 문제로 이어진다.
 * 키를 서버 환경변수로 옮기고 앱은 /map/search/place 만 호출하도록 바꾼다.
 *
 * 외부 API 가 죽어도 통합 검색의 게시글 쪽은 살아 있어야 하므로, 이 클래스는
 * 예외를 밖으로 던지지 않고 빈 목록으로 흡수한다(GeocodingObject 와 다른 점).
 */
@Component
class NaverPlaceSearchObject(
    @Value("\${custom.data.search.naver.client-id}")
    private val naverClientId: String,
    @Value("\${custom.data.search.naver.client-secret}")
    private val naverClientSecret: String,
    @Value("\${custom.data.search.ncp.api-key-id}")
    private val ncpApiKeyId: String,
    @Value("\${custom.data.search.ncp.api-key}")
    private val ncpApiKey: String,
    private val restTemplate: RestTemplate
) {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val LOCAL_SEARCH_URL = "https://openapi.naver.com/v1/search/local.json"
        private const val GEOCODING_URL = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode"
        private const val LOCAL_SEARCH_DISPLAY = 5

        /**
         * 지역검색(상호/지명) 결과가 이 개수 미만일 때만 지오코딩(주소검색)을 보충 호출한다.
         * "강남역" 같은 지명은 지역검색만으로 충분하고, "테헤란로 152" 같은 순수 주소일 때만
         * 지오코딩이 의미가 있다. 앱이 항상 둘 다 호출하던 것을 조건부로 바꿔
         * 외부 호출량(=NCP 과금)과 응답 지연을 함께 줄인다.
         */
        private const val GEOCODING_FALLBACK_THRESHOLD = 3

        /** 네이버 지역검색의 mapx/mapy 는 WGS84 좌표를 1e7 배한 정수로 내려온다. */
        private const val NAVER_COORD_SCALE = 10_000_000.0

        private val HTML_TAG = Regex("<[^>]*>")
    }

    fun search(keyword: String): List<PlaceInfo> {
        if (keyword.isBlank()) return emptyList()

        val local = searchLocal(keyword)
        val geocoded =
            if (local.size < GEOCODING_FALLBACK_THRESHOLD) searchGeocoding(keyword)
            else emptyList()

        // title 기준 중복 제거. distinctBy 는 앞선 것을 남기므로 지역검색 결과가 우선된다.
        return (local + geocoded).distinctBy { it.title }
    }

    /** 상호·지명 검색 (네이버 개발자센터 오픈API) */
    private fun searchLocal(keyword: String): List<PlaceInfo> {
        return try {
            // build().encode().toUri() 로 URI 를 넘긴다. toUriString() 으로 String 을
            // 넘기면 RestTemplate 의 DefaultUriBuilderFactory 가 한 번 더 인코딩해
            // '%'가 '%25'로 바뀌고, 한글 검색어가 전부 깨진다(결과 0건).
            val uri = UriComponentsBuilder.fromUriString(LOCAL_SEARCH_URL)
                .queryParam("query", keyword)
                .queryParam("display", LOCAL_SEARCH_DISPLAY)
                .build()
                .encode()
                .toUri()

            val headers = HttpHeaders().apply {
                set("X-Naver-Client-Id", naverClientId)
                set("X-Naver-Client-Secret", naverClientSecret)
            }

            val response = restTemplate.exchange(
                uri, HttpMethod.GET, HttpEntity<Void>(headers), String::class.java
            )

            val items = mapper.readTree(response.body).path("items")
            if (!items.isArray) return emptyList()

            items.mapNotNull { item ->
                val mapY = item.path("mapy").asText().toDoubleOrNull() ?: return@mapNotNull null
                val mapX = item.path("mapx").asText().toDoubleOrNull() ?: return@mapNotNull null
                val title = item.path("title").asText("").replace(HTML_TAG, "")
                if (title.isBlank()) return@mapNotNull null

                PlaceInfo(
                    title = title,
                    address = item.path("address").asText(""),
                    roadAddress = item.path("roadAddress").asText(""),
                    latitude = mapY / NAVER_COORD_SCALE,
                    longitude = mapX / NAVER_COORD_SCALE
                )
            }
        } catch (e: Exception) {
            logger.warn { "[장소검색] 지역검색 실패 keyword=$keyword : ${e.message}" }
            emptyList()
        }
    }

    /** 주소 검색 (네이버 클라우드 플랫폼 지오코딩) */
    private fun searchGeocoding(keyword: String): List<PlaceInfo> {
        return try {
            val uri = UriComponentsBuilder.fromUriString(GEOCODING_URL)
                .queryParam("query", keyword)
                .build()
                .encode()
                .toUri()

            val headers = HttpHeaders().apply {
                set("X-NCP-APIGW-API-KEY-ID", ncpApiKeyId)
                set("X-NCP-APIGW-API-KEY", ncpApiKey)
            }

            val response = restTemplate.exchange(
                uri, HttpMethod.GET, HttpEntity<Void>(headers), String::class.java
            )

            val addresses = mapper.readTree(response.body).path("addresses")
            if (!addresses.isArray) return emptyList()

            addresses.mapNotNull { address ->
                val y = address.path("y").asText().toDoubleOrNull() ?: return@mapNotNull null
                val x = address.path("x").asText().toDoubleOrNull() ?: return@mapNotNull null
                val roadAddress = address.path("roadAddress").asText("")
                val jibunAddress = address.path("jibunAddress").asText("")
                val title = roadAddress.ifBlank { jibunAddress }
                if (title.isBlank()) return@mapNotNull null

                PlaceInfo(
                    title = title,
                    address = jibunAddress,
                    roadAddress = roadAddress,
                    latitude = y,
                    longitude = x
                )
            }
        } catch (e: Exception) {
            logger.warn { "[장소검색] 지오코딩 실패 keyword=$keyword : ${e.message}" }
            emptyList()
        }
    }
}
