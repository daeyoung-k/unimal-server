package com.unimal.board.service.post

import com.fasterxml.jackson.databind.ObjectMapper
import com.unimal.board.controller.map.dto.MapFeedRequest
import com.unimal.board.controller.map.dto.MapFeedSectionRequest
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
 * 늘어나는 구조**로 바꿨다. 임계값을 코드에 박아두고 런타임에 판정하면 글이 쌓이는
 * 대로 저절로 전환된다.
 *
 * **쿼리는 여전히 1개다.** 섹션별 전용 SQL 을 짜지 않고 KNN 후보 [POOL_SIZE] 건을
 * 한 번 뽑아 메모리에서 나눈다. 지금 밀도(공개 글 50건 미만)에서는 KNN 80건이 사실상
 * 전체 공개 글이라 정확도 손실이 0이고, 쿼리 3개 + 인덱스 2개를 미리 만드는 건
 * 오버엔지니어링이다.
 *
 * **다만 이 근사에는 유효기간이 있다.** 글이 [POOL_SIZE] 를 크게 넘기면
 * [FeedSectionType.LATEST] 가 "전국 최신"이 아니라 "가까운 80건 중 최신"이 된다.
 * 공개 글이 수천 건이 되면 섹션별 전용 쿼리(UNION ALL 한 방)로 갈아타야 한다.
 *
 * ## 2026-07-31 개정 (1) — 섹션 단건 조회 / 캐시 우회
 *
 * 앱이 섹션별 새로고침 버튼을 갖게 되면서 [getSection] 이 생겼다.
 *
 * **섹션 하나만 요청받아도 내부에서는 전체 피드를 계산한다.** 언뜻 낭비 같지만
 * 이게 유일하게 옳은 구현이다 — [buildSections] 는 후보 풀 하나를 순서대로 `used`
 * 집합에 담아가며 나누므로 **섹션들이 서로 독립이 아니다.** 예컨대 LATEST 만 따로
 * 계산하면 앞 섹션이 이미 가져간 글을 걸러낼 근거가 없어져, 앱 화면에서 같은 글이
 * 두 섹션에 동시에 뜬다. 전체를 계산한 뒤 요청한 섹션만 잘라 내리면 **부분 갱신
 * 결과가 전체 조회 결과와 항상 일치한다.**
 *
 * 비용도 문제되지 않는다. 계산 결과는 통째로 [feedCacheKey] 에 캐시되므로 실제
 * 쿼리는 좌표 격자당 60초에 한 번이고, 줄어드는 건 응답 크기(약 1/3)다.
 *
 * **여기가 나중에 갈아탈 지점이다.** "공개 글 수천 건" 시점이 오면 [buildSections] 를
 * 섹션별 전용 쿼리로 바꾸게 되는데, 그때 [getSection] 내부만 고치면 되고 **API 계약과
 * 앱은 그대로다.**
 *
 * ## 2026-07-31 개정 (2) — 섹션별 조건 정의
 *
 * 세 섹션이 "같은 풀을 정렬만 바꿔 보여주는" 상태를 벗어나 각자 모집단을 갖는다.
 *
 * | 섹션 | 조건 | 정렬 |
 * |---|---|---|
 * | [FeedSectionType.HOT] | 사진 있음 + 반응 ≥ 1, 기간 무제한 | 시간감쇠 인기순 |
 * | [FeedSectionType.NEAR] | 5km 이내 + [RECENT_WINDOW_HOURS] 이내 | 가까운 순 |
 * | [FeedSectionType.LATEST] | [RECENT_WINDOW_HOURS] 이내 | 최신순 |
 *
 * 여기에 더해 **사진 없는 글은 리포지토리 단계에서 이미 48시간으로 잘려 온다**
 * (`MapFeedRepositoryImpl` 참고 — 마커 쿼리와 같은 규칙). 그래서 각 섹션에 "텍스트 글
 * 2일" 조건을 따로 걸 필요가 없다.
 */
@Service
class MapFeedService(
    private val mapFeedRepository: MapFeedRepositoryImpl,
    private val redisCacheManager: RedisCacheManager,
    private val hashidsUtil: HashidsUtil,
    private val objectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger {}

    fun getFeed(request: MapFeedRequest): MapFeedResponse =
        loadFeed(request.latitude, request.longitude, request.refresh)

    /**
     * 섹션 1개만 돌려준다. 해당 섹션이 이번 계산에서 만들어지지 않았으면 null.
     *
     * null 은 오류가 아니라 **정상적인 "지금은 이 섹션이 없다"** 이다. 적응형 구조라
     * 후보 풀이 [MULTI_SECTION_THRESHOLD] 아래로 떨어지거나 HOT/LATEST 가
     * [MIN_SECTION_SIZE] 를 못 채우면 섹션이 사라진다. 앱은 이때 그 섹션을 화면에서
     * 지우고 나머지는 그대로 둔다.
     */
    fun getSection(request: MapFeedSectionRequest): MapFeedSection? =
        loadFeed(request.latitude, request.longitude, request.refresh)
            .sections
            .firstOrNull { it.type == request.type }

    /**
     * 캐시 읽기 → 없으면 계산 → 캐시 쓰기. [getFeed] 와 [getSection] 의 공통 경로다.
     *
     * [refresh] 가 true 면 읽기만 건너뛴다 (쓰기는 그대로 —
     * [MapFeedRequest.refresh] 주석 참고).
     */
    private fun loadFeed(
        latitude: Double,
        longitude: Double,
        refresh: Boolean,
    ): MapFeedResponse {
        val cacheKey = feedCacheKey(latitude, longitude)

        if (!refresh) {
            readCache(cacheKey)?.let { return it }
        }

        val candidates = mapFeedRepository.findNearCandidates(
            lat = latitude,
            lng = longitude,
            // +1 은 단일 섹션 모드의 has_more 판정용. 풀을 다 쓰는 다중 섹션
            // 모드에서도 "더 있다"를 알려면 limit 를 한 칸 넘겨 받아야 한다.
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
     * 그래서 [MULTI_SECTION_THRESHOLD] 미만이면 NEAR 하나만 낸다 — 이때는 반경·기간
     * 제한을 걸지 않는다([fallbackNearSection]). 보여줄 게 적어서 안 나누기로 한
     * 상태에서 필터까지 더하면 화면이 통째로 빈다.
     *
     * ## 배정 순서 — 좁은 섹션부터 (2026-07-31 개정)
     *
     * `HOT → NEAR → LATEST`.
     *
     * **NEAR ⊂ LATEST 라는 게 핵심이다.** NEAR 는 "5km 이내 + 48시간 이내", LATEST 는
     * "48시간 이내" — NEAR 의 모든 후보가 LATEST 의 후보이기도 하다. 그래서 LATEST 를
     * 먼저 배정하면 48시간 이내 글을 [SECTION_LIMIT] 건까지 통째로 가져가고, 주변에
     * 글이 그보다 적은 흔한 상황에서 **NEAR 가 매번 빈다.** 좁은 쪽을 먼저 채워야
     * 양쪽이 다 산다.
     *
     * 대가로 **LATEST 는 실질적으로 "주변에서 이미 본 것 말고 새로 올라온 것"이 된다.**
     * 5km 밖에서 방금 올라온 글이 주로 여기 앉는데, 섹션 이름과도 어긋나지 않는다.
     * HOT 이 맨 앞인 건 개정 전과 같다 — 사진 + 반응이라는 가장 좁은 조건이라 뒤로
     * 미루면 [MIN_SECTION_SIZE] 를 못 채우고 사라진다.
     *
     * ## 아무 섹션도 못 만들면 단일 모드로 되돌린다
     *
     * [MULTI_SECTION_THRESHOLD] 판정은 **필터 걸기 전** 풀 크기로 한다. 그래서 후보가
     * 20건이어도 전부 48시간을 넘긴 사진 글이면 HOT 도 NEAR 도 LATEST 도 못 만들어져
     * `sections` 가 빈 채로 나갈 수 있다 — 앱은 그때 시트를 아예 렌더하지 않는다.
     * 마지막에 [fallbackNearSection] 으로 되돌려 **피드가 비는 경우를 없앤다.**
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
            return listOf(fallbackNearSection(ordered))
        }

        val used = mutableSetOf<Long>()
        val picked = mutableMapOf<FeedSectionType, PickedSection>()
        val recentFrom = now.minusHours(RECENT_WINDOW_HOURS)
        // `ordered` 가 거리순이므로 이 리스트도 거리순이다 — NEAR 정렬이 공짜로 나온다.
        val recent = ordered.filter { it.createdAt.isAfter(recentFrom) }

        // 1) HOT — 사진 있는 글 중 반응 1개 이상, 시간감쇠 인기순.
        //    기간 제한이 없다: 사진 글은 48시간 컷 대상이 아니고 hotScore 가 묵은 글을 알아서 민다.
        pickSection(
            source = ordered
                .filter { it.fileUrl != null && it.likeCount + it.replyCount > 0 }
                .sortedWith(
                    compareByDescending<MapFeedCandidate> { hotScore(it, now) }.thenByDescending { it.id }
                ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.HOT] = PickedSection(it, FeedSectionType.HOT.title) }

        // 2) NEAR — 5km 이내, 가까운 순. 5km 안에서 최소 건수를 못 채우면 반경을 풀어
        //    폴백하고 헤더 문구를 바꾼다. "지금 여기"라고 해놓고 30km 밖 글을 보여주면
        //    거짓말이 된다. 폴백은 minSize=1 — 여기가 피드를 비지 않게 하는 sink 다.
        val withinRadius = recent.filter { it.distanceMeters <= NEAR_RADIUS_METERS }
        val near = pickSection(withinRadius, used, MIN_SECTION_SIZE)
        if (near != null) {
            picked[FeedSectionType.NEAR] = PickedSection(near, FeedSectionType.NEAR.title)
        } else {
            pickSection(recent, used, 1)?.let {
                picked[FeedSectionType.NEAR] = PickedSection(it, NEAR_FALLBACK_TITLE)
            }
        }

        // 3) LATEST — 48시간 이내, 최신순. NEAR 가 가져가고 남은 것.
        pickSection(
            source = recent.sortedWith(
                compareByDescending<MapFeedCandidate> { it.createdAt }.thenByDescending { it.id }
            ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.LATEST] = PickedSection(it, FeedSectionType.LATEST.title) }

        // 필터를 다 통과한 섹션이 하나도 없으면 단일 모드로 되돌린다 (위 주석 참고).
        if (picked.isEmpty()) return listOf(fallbackNearSection(ordered))

        // 표시 순서 = enum 선언 순서. 순서를 바꾸려면 FeedSectionType 만 고치면 된다.
        return FeedSectionType.entries.mapNotNull { type ->
            picked[type]?.let { toSection(type, it.candidates, SECTION_LIMIT, it.title) }
        }
    }

    /**
     * 반경·기간 제한 없이 가까운 순으로 채우는 [FeedSectionType.NEAR] 단일 섹션.
     *
     * 단일 섹션 모드와 "아무 섹션도 못 만든 경우"의 공통 출구다. 헤더 문구가
     * [NEAR_FALLBACK_TITLE] 인 이유는 5km 보장이 없기 때문이다.
     */
    private fun fallbackNearSection(ordered: List<MapFeedCandidate>): MapFeedSection =
        toSection(
            FeedSectionType.NEAR,
            ordered.take(SINGLE_SECTION_LIMIT + 1),
            SINGLE_SECTION_LIMIT,
            NEAR_FALLBACK_TITLE,
        )

    /** 배정 결과 + 그때 쓸 헤더 문구. 폴백이면 enum 의 [FeedSectionType.title] 과 다르다. */
    private data class PickedSection(
        val candidates: List<MapFeedCandidate>,
        val title: String,
    )

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
        title: String = type.title,
    ) = MapFeedSection(
        type = type,
        title = title,
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
     * 사진 유무는 여기에 넣지 않는다. HOT 후보를 고를 때 이미 사진 있는 글만 남기므로
     * 점수에 보너스를 더해봐야 모든 후보가 똑같이 받아 순서가 바뀌지 않는다.
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
     *
     * 버전 프리픽스는 스키마·의미 변경 시 캐시를 통째로 무효화하기 위한 것이다.
     *
     * **`v1` → `v2` (2026-07-29):** 응답 키를 camelCase → snake_case 로 바꿨다.
     *
     * **`v2` → `v3` (2026-07-30):** 적응형 섹션 도입.
     *
     * **`v3` → `v4` (2026-07-31):** 섹션별 조건(사진·반경·기간)과 배정 순서를 바꿨다.
     * JSON 스키마는 그대로지만 **같은 좌표에서 나오는 섹션 구성이 완전히 달라진다.**
     * 버전을 안 올리면 배포 후 60초간 옛 구성이 섞여 나와 "왜 NEAR 가 5km 를 안 지키지"로
     * 헛디버깅하게 된다. 캐시 무효화는 싸고 혼란은 비싸다.
     *
     * 격자 좌표는 키에 그대로 남는다 — 섹션 단건 조회도 이 캐시를 읽으므로 전체/부분이
     * 같은 값을 본다.
     */
    private fun feedCacheKey(lat: Double, lng: Double): String =
        "map:feed:v4:${snap(lat)}:${snap(lng)}"

    // Locale 을 고정하지 않으면 환경에 따라 소수점이 ',' 가 되어 캐시 키가 달라진다.
    private fun snap(value: Double): String = String.format(Locale.ROOT, "%.3f", value)

    /**
     * 캐시는 **JSON 문자열**로 저장한다 ([RedisCacheManager.setStringCacheSeconds]).
     *
     * `setAnyCacheSeconds` 를 쓰면 안 된다. `RedisConfig` 의
     * `GenericJackson2JsonRedisSerializer()` 는 no-arg 생성이라 `JavaTimeModule` 이 없고,
     * [MapFeedCard.createdAt] 같은 `LocalDateTime` 을 만나면 런타임에 터진다.
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
         * 굶지 않으려면 여유가 필요하다. 80 은 HOT/NEAR/LATEST 필터를 통과 못 하는 글까지
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
         * 다중 모드보다 넉넉하게 준다.
         */
        private const val SINGLE_SECTION_LIMIT = 20

        /**
         * 섹션이 살아남기 위한 최소 건수. 2 장짜리 캐러셀은 섹션 헤더 값을 못 한다.
         * NEAR 반경 폴백에는 적용하지 않는다 (sink).
         */
        private const val MIN_SECTION_SIZE = 3

        /**
         * 이 건수 미만이면 섹션을 나누지 않고 NEAR 하나만 낸다.
         *
         * **필터 걸기 전** 풀 크기로 판정한다. 사진·반경·기간 조건을 다 적용한 뒤에
         * 판정하면 계산을 두 번 하게 되고, 어차피 다 걸러졌을 때의 안전망은
         * [buildSections] 끝의 `picked.isEmpty()` 가 맡는다.
         */
        private const val MULTI_SECTION_THRESHOLD = 20

        /**
         * [FeedSectionType.LATEST] / [FeedSectionType.NEAR] 의 시간 윈도우.
         *
         * 48시간이다. **리포지토리의 "사진 없는 글 48시간 컷"과 같은 값이어야 한다** —
         * 다르면 "마커에는 없는데 피드에는 있는 글" 또는 그 반대가 생긴다.
         * `MapFeedRepositoryImpl` 의 SQL 에 `INTERVAL '48 hours'` 로 박혀 있으니
         * 한쪽을 바꾸면 반드시 다른 쪽도 본다.
         *
         * 개정 전에는 7일이었다. 하루에 글이 몇 건 안 올라와서 48시간을 걸면 LATEST 가
         * 거의 항상 [MIN_SECTION_SIZE] 미달로 사라졌기 때문인데, 지금은 배정 순서가
         * `NEAR → LATEST` 라 NEAR 가 먼저 채워지고 LATEST 가 사라져도 피드는 멀쩡하다.
         */
        private const val RECENT_WINDOW_HOURS = 48L

        /**
         * [FeedSectionType.NEAR] 의 반경. "진짜 내 주변"의 기준이다.
         *
         * 5km 는 지도 줌 14(반경 5km)와 같은 값이라, 사용자가 기본 줌에서 보는 마커
         * 범위와 NEAR 섹션의 범위가 대체로 겹친다. 지도에 보이는 핀이 아래 피드에도
         * 있어야 두 목록이 같은 곳을 말하는 것으로 읽힌다.
         */
        private const val NEAR_RADIUS_METERS = 5_000.0

        /**
         * 반경 폴백 시 [FeedSectionType.NEAR] 헤더 문구.
         *
         * 5km 안이 비어 범위를 넓혔을 때 "지금 여기 이야기"를 그대로 쓰면 30km 밖 글을
         * 가리키는 거짓말이 된다. 문구가 응답에 실려 오므로 앱은 고칠 게 없다.
         */
        private const val NEAR_FALLBACK_TITLE = "가까운 이야기"

        /**
         * 새 글이 최대 1분 늦게 보인다. 지도 피드에서 그 정도 지연은 체감되지 않고,
         * 대신 지도를 흔드는 동안의 반복 요청을 전부 흡수한다.
         *
         * 수동 새로고침은 [MapFeedRequest.refresh] 로 이 캐시를 우회한다 — 사용자가
         * 명시적으로 요청한 갱신까지 1분 묶어두면 버튼이 고장난 것처럼 보인다.
         */
        private const val FEED_CACHE_TTL_SECONDS = 60L
    }
}
