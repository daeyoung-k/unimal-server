package com.unimal.board.service.post

import com.fasterxml.jackson.databind.ObjectMapper
import com.unimal.board.controller.map.dto.MapFeedRequest
import com.unimal.board.domain.board.map.MapFeedCandidate
import com.unimal.board.domain.board.map.MapFeedRepositoryImpl
import com.unimal.board.service.post.dto.map.MapFeedCard
import com.unimal.board.service.post.dto.map.MapFeedResponse
import com.unimal.board.service.post.dto.map.MapFeedSection
import com.unimal.board.service.post.enums.FeedSectionType
import com.unimal.board.utils.HashidsUtil
import com.unimal.board.utils.RedisCacheManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.Locale

/**
 * 지도 바텀카드 피드 조립.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md`
 *
 * [MapPostService] 에 넣지 않았다. 마커 조회와 피드 조립은 책임이 다르고
 * [MapPostService] 는 이미 파일 매칭 로직으로 비대하다.
 */
@Service
class MapFeedService(
    private val mapFeedRepository: MapFeedRepositoryImpl,
    private val redisCacheManager: RedisCacheManager,
    private val hashidsUtil: HashidsUtil,
    private val objectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger {}

    fun getFeed(request: MapFeedRequest): MapFeedResponse {
        val cacheKey = feedCacheKey(request.latitude, request.longitude)

        readCache(cacheKey)?.let { return it }

        val candidates = mapFeedRepository.findNearCandidates(
            lat = request.latitude,
            lng = request.longitude,
            limit = SECTION_LIMIT + 1,
        )

        val response = MapFeedResponse(sections = buildSections(candidates))

        writeCache(cacheKey, response)
        return response
    }

    /**
     * 지금은 [FeedSectionType.NEAR] 하나뿐이라 섹션 간 중복 제거가 없다.
     *
     * 섹션이 2개 이상으로 늘어나면 `used: MutableSet<Long>` 을 도입한다. 우선순위는
     * `NEAR` > `LATEST` > `HOT`, 중복 판정은 **Hashids 인코딩 전 Long id** 로 한다
     * (문자열 비교 O(n×m) 를 반복하지 않기 위해).
     */
    private fun buildSections(candidates: List<MapFeedCandidate>): List<MapFeedSection> {
        // distance tie-break 은 여기서 한다 — SQL 에 2차 정렬 키를 넣으면 KNN 조기 종료가 깨진다.
        val ordered = candidates.sortedWith(
            compareBy<MapFeedCandidate> { it.distanceMeters }.thenByDescending { it.id }
        )

        val items = ordered.take(SECTION_LIMIT).map { it.toCard() }
        // 항목이 1건이라도 있으면 내려보낸다. 초안의 "3건 미만 숨김" 문턱은 지금 밀도에서
        // 피드를 백지로 만들기 때문에 폐기했다.
        if (items.isEmpty()) return emptyList()

        return listOf(
            MapFeedSection(
                type = FeedSectionType.NEAR,
                title = FeedSectionType.NEAR.title,
                hasMore = ordered.size > SECTION_LIMIT,
                items = items,
            )
        )
    }

    private fun MapFeedCandidate.toCard() = MapFeedCard(
        boardId = hashidsUtil.encode(id),
        // 400px 파생 우선. 백필 전 기존 파일은 thumb_url 이 null 이라 원본으로 폴백한다.
        thumbnailUrl = thumbUrl ?: fileUrl,
        imageUrl = fileUrl,
        title = title,
        content = content,
        streetName = streetName,
        dong = dong,
        latitude = latitude,
        longitude = longitude,
        distanceMeters = distanceMeters.toInt(),
        nickname = nickname ?: "",
        profileImage = profileImage,
        likeCount = likeCount,
        replyCount = replyCount,
        createdAt = createdAt,
    )

    /**
     * 좌표를 소수점 3자리(약 110m 격자)로 스냅해 캐시 키를 만든다.
     *
     * 지도 좌표는 소수점 7자리로 연속 변하므로 그대로 키에 쓰면 히트율이 0이다.
     * 격자에 스냅하면 같은 블록 안에서 지도를 조금씩 움직이는 동안 같은 키를 맞는다.
     *
     * `zoom` 은 키에 넣지 않는다 — 쿼리에 영향이 없으므로 넣으면 히트율만 깎인다.
     * 반경 캡을 도입하는 시점에 다시 넣어야 한다.
     *
     * 버전 프리픽스는 스키마 변경 시 캐시를 통째로 무효화하기 위한 것이다. 버전이 없으면
     * 배포 후 옛 JSON 이 역직렬화 실패로 500 을 낸다.
     *
     * **`v1` → `v2` (2026-07-29):** 응답 키를 camelCase → snake_case 로 바꿨다.
     * 버전을 안 올리면 배포 직후 60초간 옛 camelCase JSON 을 읽는데, Kotlin non-null
     * 프로퍼티가 채워지지 않아 역직렬화가 깨진다. (`readCache` 가 예외를 삼켜 DB 로
     * 폴백하므로 500 은 안 나지만, 캐시를 못 쓰는 구간이 생긴다.)
     */
    private fun feedCacheKey(lat: Double, lng: Double): String =
        "map:feed:v2:${snap(lat)}:${snap(lng)}"

    // Locale 을 고정하지 않으면 환경에 따라 소수점이 ',' 가 되어 캐시 키가 달라진다.
    private fun snap(value: Double): String = String.format(Locale.ROOT, "%.3f", value)

    /**
     * 캐시는 **JSON 문자열**로 저장한다 ([RedisCacheManager.setStringCacheSeconds]).
     *
     * `setAnyCacheSeconds` 를 쓰면 안 된다. `RedisConfig` 의
     * `GenericJackson2JsonRedisSerializer()` 는 no-arg 생성이라 `JavaTimeModule` 이 없고,
     * [MapFeedCard.createdAt] 같은 `LocalDateTime` 을 만나면 런타임에 터진다.
     * 기존 board 캐시가 `Long` 카운트만 담아서 아직 드러나지 않은 함정이다.
     *
     * 주입받은 [ObjectMapper] 는 Spring Boot 가 구성한 것이라 `JavaTimeModule` 이 등록돼 있고
     * `WRITE_DATES_AS_TIMESTAMPS` 가 꺼져 있어 REST 응답과 동일한 ISO-8601 문자열을 쓴다.
     */
    private fun writeCache(key: String, response: MapFeedResponse) {
        runCatching {
            redisCacheManager.setStringCacheSeconds(
                key,
                objectMapper.writeValueAsString(response),
                FEED_CACHE_TTL_SECONDS,
            )
        }.onFailure {
            // 캐시 실패로 피드 응답을 죽이지 않는다.
            logger.warn(it) { "map feed cache write failed - key=$key" }
        }
    }

    private fun readCache(key: String): MapFeedResponse? =
        runCatching {
            redisCacheManager.getCache(key)?.let {
                objectMapper.readValue(it, MapFeedResponse::class.java)
            }
        }.getOrElse {
            // 스키마가 바뀐 옛 JSON 이 남아 있어도 500 을 내지 않고 DB 조회로 넘어간다.
            logger.warn(it) { "map feed cache read failed - key=$key" }
            null
        }

    companion object {
        /** 섹션당 노출 건수. 세로 스크롤 단일 리스트라 초안(10)보다 늘렸다. */
        private const val SECTION_LIMIT = 20

        /**
         * 새 글이 최대 1분 늦게 보인다. 지도 피드에서 그 정도 지연은 체감되지 않고,
         * 대신 지도를 흔드는 동안의 반복 요청을 전부 흡수한다.
         */
        private const val FEED_CACHE_TTL_SECONDS = 60L
    }
}
