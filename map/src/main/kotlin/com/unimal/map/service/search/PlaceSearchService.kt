package com.unimal.map.service.search

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.unimal.map.service.search.dto.PlaceInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 장소 검색 + Redis 캐싱.
 *
 * 지도 화면에서 한 글자 칠 때마다(디바운스 후) 호출되는 자리라 캐시가 없으면
 * 외부 호출이 그대로 쌓인다. 상호/주소는 거의 변하지 않으므로 캐시 적중률이 높다.
 */
@Service
class PlaceSearchService(
    private val naverPlaceSearchObject: NaverPlaceSearchObject,
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val CACHE_PREFIX = "map:search:place:"

        /**
         * 6시간. 신규 매장 반영 지연은 무시할 수준이면서 외부 호출량은 크게 준다.
         */
        private val CACHE_TTL: Duration = Duration.ofHours(6)

        /** 앱에서도 2자 미만은 막지만, 서버가 최종 방어선이다. */
        private const val MIN_QUERY_LENGTH = 2
    }

    fun search(query: String): List<PlaceInfo> {
        val keyword = query.trim()
        if (keyword.length < MIN_QUERY_LENGTH) return emptyList()

        val cacheKey = CACHE_PREFIX + keyword.lowercase()
        readCache(cacheKey)?.let { return it }

        val results = naverPlaceSearchObject.search(keyword)
        writeCache(cacheKey, results)
        return results
    }

    private fun readCache(key: String): List<PlaceInfo>? {
        return try {
            stringRedisTemplate.opsForValue().get(key)?.let { mapper.readValue<List<PlaceInfo>>(it) }
        } catch (e: Exception) {
            // 캐시는 부가 기능 — 레디스가 죽어도 검색 자체는 되어야 한다.
            logger.warn { "[장소검색] 캐시 조회 실패 key=$key : ${e.message}" }
            null
        }
    }

    private fun writeCache(key: String, results: List<PlaceInfo>) {
        // 빈 결과는 캐싱하지 않는다. 외부 API 의 일시 장애를 6시간 동안 굳혀버린다.
        if (results.isEmpty()) return
        try {
            stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(results), CACHE_TTL)
        } catch (e: Exception) {
            logger.warn { "[장소검색] 캐시 저장 실패 key=$key : ${e.message}" }
        }
    }
}
