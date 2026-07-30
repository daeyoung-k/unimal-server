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
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.pow

/**
 * 지도 바텀카드 피드 조립.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md`
 *
 * [MapPostService] 에 넣지 않았다. 마커 조회와 피드 조립은 책임이 다르고
 * [MapPostService] 는 이미 파일 매칭 로직으로 비대하다.
 *
 * ## 2026-07-30 개정 — 적응형 섹션
 *
 * 섹션을 [FeedSectionType.NEAR] 하나로 고정하던 것을 **후보 풀 크기에 따라 자동으로
 * 늘어나는 구조**로 바꿨다. 스펙 §2 는 "공개 글 1,200건쯤 되면 LATEST/HOT 을 추가"라고
 * 적어뒀는데, 그 방식은 **그날 코드를 고치러 돌아와야 한다**는 뜻이다. 임계값을 코드에
 * 박아두고 런타임에 판정하면 글이 쌓이는 대로 저절로 전환된다.
 *
 * **쿼리는 여전히 1개다.** 섹션별 전용 SQL 을 짜지 않고 KNN 후보 [POOL_SIZE] 건을
 * 한 번 뽑아 메모리에서 나눈다. 지금 밀도(공개 글 50건 미만)에서는 KNN 80건이 사실상
 * 전체 공개 글이라 정확도 손실이 0이고, 쿼리 3개 + 인덱스 2개를 미리 만드는 건
 * 오버엔지니어링이다.
 *
 * **다만 이 근사에는 유효기간이 있다.** 글이 [POOL_SIZE] 를 크게 넘기면
 * [FeedSectionType.LATEST] 가 "전국 최신"이 아니라 "가까운 80건 중 최신"이 된다.
 * 부산에 방금 올라온 글이 서울 사용자의 LATEST 에 안 뜬다는 뜻이다. 공개 글이
 * 수천 건이 되면 섹션별 전용 쿼리(UNION ALL 한 방)로 갈아타야 한다.
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
            // +1 은 NEAR 섹션의 has_more 판정용. 풀을 다 쓰는 다중 섹션 모드에서도
            // "더 있다"를 알려면 limit 를 한 칸 넘겨 받아야 한다.
            limit = POOL_SIZE + 1,
        )

        val response = MapFeedResponse(sections = buildSections(candidates, LocalDateTime.now()))

        writeCache(cacheKey, response)
        return response
    }

    /**
     * 후보 풀을 섹션으로 나눈다.
     *
     * [now] 를 파라미터로 받는 이유: `LocalDateTime.now()` 를 내부에서 부르면
     * 시간감쇠([hotScore])와 최신 윈도우를 테스트할 방법이 없다. Clock 빈을 주입할
     * 만큼의 일은 아니라 인자 하나로 끝낸다.
     *
     * ## 왜 임계값 이하에서는 섹션을 나누지 않나
     *
     * 섹션 분리는 "각 섹션이 서로 다른 발견 경험을 준다"는 전제에서만 값을 한다.
     * 풀이 작으면 세 섹션이 같은 풀을 나눠 갖고, 중복 제거까지 걸리면 섹션당 1~2장이
     * 남는다. **가로 캐러셀에 카드 한 장은 단일 섹션 20장보다 초라하다.**
     * 그래서 [MULTI_SECTION_THRESHOLD] 미만이면 예전 동작 그대로 NEAR 하나만 낸다.
     *
     * ## 배정 순서 ≠ 표시 순서
     *
     * 배정은 **희소한 섹션부터** (HOT → LATEST → NEAR). HOT 은 반응 있는 글만 담아서
     * 후보가 가장 적은데, 뒤로 미루면 앞 섹션이 그 글들을 먼저 가져가 HOT 이
     * [MIN_SECTION_SIZE] 를 못 채우고 통째로 사라진다.
     *
     * 대가로 **방금 올라온 인기 글은 LATEST 가 아니라 HOT 에 뜬다.** 둘 다 자연스러운
     * 자리라 감수한다. 표시 순서는 [FeedSectionType] 선언 순서(LATEST → HOT → NEAR)다.
     *
     * NEAR 는 **sink 다.** 최소 건수를 적용하지 않는다 — 여기서 드롭하면 앞 섹션에
     * 배정되지 못한 글이 피드에서 아예 증발한다.
     */
    private fun buildSections(
        candidates: List<MapFeedCandidate>,
        now: LocalDateTime,
    ): List<MapFeedSection> {
        // distance tie-break 은 여기서 한다 — SQL 에 2차 정렬 키를 넣으면 KNN 조기 종료가 깨진다.
        val ordered = candidates.sortedWith(
            compareBy<MapFeedCandidate> { it.distanceMeters }.thenByDescending { it.id }
        )
        if (ordered.isEmpty()) return emptyList()

        if (ordered.size < MULTI_SECTION_THRESHOLD) {
            return listOf(
                toSection(FeedSectionType.NEAR, ordered.take(SINGLE_SECTION_LIMIT + 1), SINGLE_SECTION_LIMIT)
            )
        }

        val used = mutableSetOf<Long>()
        val picked = mutableMapOf<FeedSectionType, List<MapFeedCandidate>>()

        // 1) HOT — 반응 1개 이상, 시간감쇠 인기순. 후보가 가장 적으므로 먼저 배정한다.
        pickSection(
            source = ordered
                .filter { it.likeCount + it.replyCount > 0 }
                .sortedWith(
                    compareByDescending<MapFeedCandidate> { hotScore(it, now) }.thenByDescending { it.id }
                ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.HOT] = it }

        // 2) LATEST — 최근 윈도우 안, 최신순. 윈도우가 없으면 NEAR 와 모집단이 완전히
        //    같아져서 "같은 글이 순서만 다르게" 두 번 나열되는 꼴이 된다.
        val latestFrom = now.minusDays(LATEST_WINDOW_DAYS)
        pickSection(
            source = ordered
                .filter { it.createdAt.isAfter(latestFrom) }
                .sortedWith(
                    compareByDescending<MapFeedCandidate> { it.createdAt }.thenByDescending { it.id }
                ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.LATEST] = it }

        // 3) NEAR — 남은 전부, 거리순. minSize=1 (sink 이므로 드롭하지 않는다).
        pickSection(source = ordered, used = used, minSize = 1)
            ?.let { picked[FeedSectionType.NEAR] = it }

        // 표시 순서 = enum 선언 순서. 순서를 바꾸려면 FeedSectionType 만 고치면 된다.
        return FeedSectionType.entries.mapNotNull { type ->
            picked[type]?.let { toSection(type, it, SECTION_LIMIT) }
        }
    }

    /**
     * [source] 에서 아직 안 쓴 후보를 `SECTION_LIMIT + 1` 건까지 집는다.
     * [minSize] 를 못 채우면 null 을 돌려주고 **아무것도 [used] 에 넣지 않는다** —
     * 드롭된 섹션이 글을 물고 사라지면 그 글이 뒤 섹션에도 안 나온다.
     *
     * 마지막 +1 건은 has_more 판정용 탐침이라 [used] 에 넣지 않는다. 화면에 안 나오는
     * 글을 소비 처리하면 뒤 섹션에서 정당하게 쓸 기회를 뺏는다.
     */
    private fun pickSection(
        source: List<MapFeedCandidate>,
        used: MutableSet<Long>,
        minSize: Int,
    ): List<MapFeedCandidate>? {
        val taken = source.asSequence()
            .filter { it.id !in used }
            .take(SECTION_LIMIT + 1)
            .toList()
        if (taken.size < minSize) return null
        taken.take(SECTION_LIMIT).forEach { used += it.id }
        return taken
    }

    private fun toSection(
        type: FeedSectionType,
        taken: List<MapFeedCandidate>,
        limit: Int,
    ) = MapFeedSection(
        type = type,
        title = type.title,
        // `limit + 1` 건을 받아 초과분 존재로 판정한다. 별도 COUNT(*) 를 돌리지 않는다 —
        // 총 개수는 UI에 안 쓰이는데 count 쿼리는 비싸다.
        hasMore = taken.size > limit,
        items = taken.take(limit).map { it.toCard() },
    )

    /**
     * Hacker News 계열 시간감쇠 인기 점수 (스펙 §2).
     *
     * ```
     * hot = (likes × 2 + replies × 3) / (경과시간h + 2)^1.5
     * ```
     *
     * - 댓글(3)을 좋아요(2)보다 높게 본다. 댓글은 비용이 큰 참여다.
     * - 분모 `+2`: 방금 올라온 글의 점수가 발산하는 걸 막는다.
     * - 지수 `1.5`: 하루 지나면 약 1/9 로 떨어져 어제 인기 글이 상단을 영구 점유하지 못한다.
     *
     * 서버 시계가 뒤로 튀거나 미래 `created_at` 이 섞여도 음수 시간이 되지 않게 0 으로 자른다.
     */
    private fun hotScore(candidate: MapFeedCandidate, now: LocalDateTime): Double {
        val hours = Duration.between(candidate.createdAt, now)
            .toMinutes()
            .coerceAtLeast(0) / 60.0
        val weighted = candidate.likeCount * 2.0 + candidate.replyCount * 3.0
        return weighted / (hours + 2).pow(1.5)
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
     *
     * **`v2` → `v3` (2026-07-30):** 적응형 섹션 도입. JSON 스키마 자체는 호환되지만
     * (`sections` 배열 그대로), 버전을 올리지 않으면 배포 후 60초간 옛 단일 섹션 응답이
     * 섞여 나와 "섹션이 왜 안 늘었지"로 헛디버깅하게 된다. 캐시 무효화는 싸고 혼란은 비싸다.
     */
    private fun feedCacheKey(lat: Double, lng: Double): String =
        "map:feed:v3:${snap(lat)}:${snap(lng)}"

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
        /**
         * KNN 후보 조회 건수. 세 섹션이 나눠 갖는 공용 풀이다.
         *
         * `SECTION_LIMIT(10) × 3 = 30` 이면 딱 맞아떨어지지만, 중복 제거로 뒤 섹션이
         * 굶지 않으려면 여유가 필요하다. 80 은 LATEST/HOT 필터를 통과 못 하는 글까지
         * 감안한 값이다. 한 행이 200~300 bytes 라 80행은 여전히 가볍다.
         */
        private const val POOL_SIZE = 80

        /**
         * 다중 섹션일 때 섹션당 노출 건수.
         *
         * 앱은 가로 캐러셀(`MapFeedSectionRow`)로 그린다. 한 화면에 2.5장쯤 보이므로
         * 20장은 아무도 끝까지 안 넘긴다. 게다가 3섹션 × 20 = 60장이면 썸네일 60개
         * 요청이 한 번에 나간다. 10장이면 "끝이 보이는" 길이라 오히려 다 넘겨본다.
         */
        private const val SECTION_LIMIT = 10

        /**
         * 단일 [FeedSectionType.NEAR] 모드일 때의 건수. 세로가 아니라 가로 한 줄뿐이라
         * 다중 모드보다 넉넉하게 준다 — 개정 전 동작(20)을 그대로 유지한다.
         */
        private const val SINGLE_SECTION_LIMIT = 20

        /**
         * 섹션이 살아남기 위한 최소 건수. 2 장짜리 캐러셀은 섹션 헤더 값을 못 한다.
         * [FeedSectionType.NEAR] 에는 적용하지 않는다 (sink).
         */
        private const val MIN_SECTION_SIZE = 3

        /**
         * 이 건수 미만이면 섹션을 나누지 않고 NEAR 하나만 낸다.
         *
         * `MIN_SECTION_SIZE(3) + MIN_SECTION_SIZE(3) + 여유` 로 잡았다. 20건이면
         * 최악의 경우에도 HOT 3 + LATEST 3 + NEAR 14 로 세 섹션이 형태를 갖춘다.
         *
         * **이 값 하나가 "지금 초라해 보이는가"를 결정한다.** 공개 글이 늘어 지도
         * 어디를 찍어도 20건이 잡히면 저절로 3섹션이 된다. 반대로 화면을 지금 당장
         * 채우고 싶다고 이 값을 낮추면, 카드 한두 장짜리 섹션이 늘어서 오히려 빈약해 보인다.
         */
        private const val MULTI_SECTION_THRESHOLD = 20

        /**
         * [FeedSectionType.LATEST] 의 시간 윈도우.
         *
         * 7일이다. 스펙 초안은 48시간을 상정했지만, 지금은 하루에 글이 몇 건 안 올라와서
         * 48시간을 걸면 LATEST 가 거의 항상 [MIN_SECTION_SIZE] 미달로 사라진다.
         * **밀도가 오르면 좁혀야 하는 값이다** — 글이 많은데 윈도우가 넓으면
         * "방금 올라온 소식"에 일주일 전 글이 앉는다.
         */
        private const val LATEST_WINDOW_DAYS = 7L

        /**
         * 새 글이 최대 1분 늦게 보인다. 지도 피드에서 그 정도 지연은 체감되지 않고,
         * 대신 지도를 흔드는 동안의 반복 요청을 전부 흡수한다.
         */
        private const val FEED_CACHE_TTL_SECONDS = 60L
    }
}
