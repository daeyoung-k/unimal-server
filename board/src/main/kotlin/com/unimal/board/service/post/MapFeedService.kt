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
 * ## 섹션 구성 (2026-08-06 개정)
 *
 * 세 섹션을 항상 시도하고, [MIN_SECTION_SIZE] 를 못 채운 섹션만 빠진다. 풀 크기로
 * "단일 섹션 모드 / 다중 섹션 모드"를 가르던 임계값은 없앴다 — 그 모드가 반경·기간
 * 제한을 통째로 풀어버려 저밀도 지역에서 100km 밖 글이 "가까운 이야기"로 나왔다.
 * 자세한 경위는 [buildSections] KDoc.
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
 * | [FeedSectionType.NEAR] | [NEAR_RADIUS_METERS] 이내, 기간 무제한 | 가까운 순 |
 * | [FeedSectionType.LATEST] | [RECENT_WINDOW_HOURS] 이내, 거리 무제한 | 최신순 |
 * | [FeedSectionType.ALL] | 사진 있음 + 반응 ≥ 1, 거리·기간 무제한 | 시간감쇠순 |
 *
 * 표의 순서는 표시 순서다. 배정 순서는 다르다([buildSections]).
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
     * null 은 오류가 아니라 **정상적인 "지금은 이 섹션이 없다"** 이다. 세 섹션 모두
     * [MIN_SECTION_SIZE] 를 못 채우면 사라지고, NEAR 는 [NEAR_RADIUS_METERS] 안에서
     * 못 채우면 반경을 풀지 않고 그냥 사라진다. 앱은 이때 그 섹션을 화면에서
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
            // 여유 1건. 단일 섹션 모드가 있던 시절의 has_more 판정 잔재지만, 섹션별
            // has_more 는 pickSection 이 자체적으로 SECTION_LIMIT + 1 로 재므로
            // 지금은 순수한 여유분이다. 한 행이 200~300 bytes 라 두고 손해는 없다.
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
     * ## 조건을 못 채운 섹션은 그냥 뺀다 (2026-08-06 개정)
     *
     * 예전에는 "피드가 절대 비면 안 된다"를 우선해 두 겹의 폴백을 뒀다.
     * (1) 5km 안이 부족하면 NEAR 의 반경을 풀고 헤더만 "가까운 이야기"로 교체,
     * (2) 후보가 20건 미만이면 반경·기간 없이 가까운 순 20건을 단일 섹션으로.
     *
     * 저밀도 지역에서 이게 그대로 드러났다 — 서산에서 앱을 열면 100km 넘게 떨어진 글이
     * "가까운 이야기"라는 이름으로 나왔다. 헤더를 바꿔도 "가까운"이라는 단어가 남는 한
     * 거짓말이고, 사용자는 카드의 `112km` 를 보고서야 알아챈다.
     *
     * 그래서 **거리·시간 약속을 못 지키면 섹션을 내리지 않는다.** 빈 피드보다 거짓 피드가
     * 나쁘다. 섹션이 하나도 안 만들어지면 응답의 `sections` 가 비고, 앱은 시트를 아예
     * 렌더하지 않는다(`map_feed_sheet.dart` — "올릴 게 없으면 없는 것이 맞다").
     * 실제로 완전히 비는 경우는 드물다 — [FeedSectionType.LATEST] 는 반경 제한이 없어
     * 전국에 48시간 이내 글이 [MIN_SECTION_SIZE] 건만 있으면 살아남는다.
     *
     * ## 배정 순서 — 복구 불가능한 섹션부터 (2026-08-06 개정)
     *
     * `NEAR → ALL → LATEST`.
     *
     * **NEAR 와 LATEST 는 크게 겹친다.** NEAR 는 "5km 이내"(기간 무관), LATEST 는
     * "48시간 이내"(거리 무관) — 5km 안에 방금 올라온 글은 양쪽 다 해당한다. 주변에
     * 글이 적을 때 LATEST 를 먼저 배정하면 그 겹치는 글을 [SECTION_LIMIT] 건까지
     * 통째로 가져가고 **NEAR 가 매번 빈다.**
     *
     * 개정 전에는 이 섹션(당시 이름 `HOT`)이 맨 앞이었다. "사진 + 반응이라는 가장 좁은 조건"이라는 이유였는데,
     * 그건 NEAR 에 반경 폴백이라는 안전망이 있을 때 이야기다. 폴백을 없앤 지금은
     * **ALL 이 NEAR 를 굶길 수 있다.** ALL 의 모집단은 전국이고 `hotScore` 가 시간감쇠라
     * 최근 글을 선호하는데, 그게 정확히 NEAR 의 후보(5km + 48시간)와 겹친다. 동네에
     * 사진+반응 글이 10건 있으면 ALL 이 전부 가져가고 NEAR 는 3건을 못 채워 사라진다.
     * 표시 순서에서 맨 위로 올린 섹션이 가장 잘 사라지는 셈이라 앞뒤가 안 맞는다.
     *
     * 그래서 **되돌릴 수 없는 쪽을 먼저 채운다.** NEAR 는 5km 밖에서 보충할 방법이
     * 아예 없지만, ALL 은 모집단이 전국이라 근처 몇 건을 뺏겨도 대체 후보가 있다.
     *
     * 대가로 **LATEST 는 실질적으로 "주변에서 이미 본 것 말고 새로 올라온 것"이 된다.**
     * 5km 밖에서 방금 올라온 글이 주로 여기 앉는데, 섹션 이름과도 어긋나지 않는다.
     *
     * (배정 순서는 여기, 표시 순서는 [FeedSectionType] 선언 순서다. 서로 다르다.)
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

        val used = mutableSetOf<Long>()
        val picked = mutableMapOf<FeedSectionType, List<MapFeedCandidate>>()
        val recentFrom = now.minusHours(RECENT_WINDOW_HOURS)
        // LATEST 전용 모집단. NEAR 는 기간을 안 걸므로 `ordered` 를 직접 쓴다.
        val recent = ordered.filter { it.createdAt.isAfter(recentFrom) }

        // 1) NEAR("가까운 스토리") — 5km 이내, 가까운 순. 가장 먼저 배정한다(위 KDoc).
        //    못 채우면 **섹션을 만들지 않는다.** 반경을 푸는 폴백은 2026-08-06 에 제거했다.
        //    "가까운"이라는 이름을 달고 100km 밖 글을 내보내지 않는다.
        //
        //    `recent` 가 아니라 `ordered` 에서 뽑는다 — 이름이 거리만 약속하므로 기간도
        //    걸지 않는다. 걸어두면 447m 짜리 3일 전 글이 여기가 아니라 ALL 로 새는데,
        //    사용자 눈엔 "가까운 글이 전국 스토리에 있는" 버그로 보인다.
        pickSection(
            source = ordered.filter { it.distanceMeters <= NEAR_RADIUS_METERS },
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.NEAR] = it }

        // 2) ALL("전국 스토리") — 사진 있는 글 중 반응 1개 이상, 시간감쇠 순. 전국 대상.
        //    기간 제한이 없다: 사진 글은 48시간 컷 대상이 아니고 hotScore 가 묵은 글을 알아서 민다.
        //    NEAR 가 가져간 근처 글은 여기서 빠지므로, 이름대로 "전국"에 가까워진다.
        pickSection(
            source = ordered
                .filter { it.fileUrl != null && it.likeCount + it.replyCount > 0 }
                .sortedWith(
                    compareByDescending<MapFeedCandidate> { hotScore(it, now) }.thenByDescending { it.id }
                ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.ALL] = it }

        // 3) LATEST("방금 올라온 스토리") — 48시간 이내, 최신순. NEAR 가 가져가고 남은 것.
        pickSection(
            source = recent.sortedWith(
                compareByDescending<MapFeedCandidate> { it.createdAt }.thenByDescending { it.id }
            ),
            used = used,
            minSize = MIN_SECTION_SIZE,
        )?.let { picked[FeedSectionType.LATEST] = it }

        // 표시 순서 = enum 선언 순서. 순서를 바꾸려면 FeedSectionType 만 고치면 된다.
        // 헤더 문구도 enum 이 단일 출처다 — 서비스가 갈아끼우는 경로는 이제 없다.
        return FeedSectionType.entries.mapNotNull { type ->
            picked[type]?.let { toSection(type, it) }
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

    /**
     * 헤더 문구는 [FeedSectionType.title] 이 단일 출처다. 서비스가 갈아끼우는 경로는
     * 2026-08-06(반경 폴백 제거) 이후로 없다 — 다시 만들지 말 것. 문구가 두 곳에서
     * 나오기 시작하면 "이 섹션 제목이 어디서 오는지" 추적이 안 된다.
     */
    private fun toSection(
        type: FeedSectionType,
        taken: List<MapFeedCandidate>,
    ) = MapFeedSection(
        type = type,
        title = type.title,
        // `SECTION_LIMIT + 1` 건을 받아 초과분 존재로 판정한다. 별도 COUNT(*) 를 돌리지
        // 않는다 — 총 개수는 UI에 안 쓰이는데 count 쿼리는 비싸다.
        hasMore = taken.size > SECTION_LIMIT,
        items = taken.take(SECTION_LIMIT).map { it.toCard() },
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
     * 사진 유무는 여기에 넣지 않는다. ALL 후보를 고를 때 이미 사진 있는 글만 남기므로
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
        "map:feed:v5:${snap(lat)}:${snap(lng)}"

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
         * 굶지 않으려면 여유가 필요하다. 80 은 ALL/NEAR/LATEST 필터를 통과 못 하는 글까지
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
         * 섹션이 살아남기 위한 최소 건수. 2 장짜리 캐러셀은 섹션 헤더 값을 못 한다.
         *
         * 예외 없이 세 섹션에 모두 적용한다. 2026-08-06 이전에는 NEAR 반경 폴백만
         * `minSize = 1` 로 빠져나가는 sink 였는데, 그 폴백 자체를 없앴다.
         */
        private const val MIN_SECTION_SIZE = 3

        /**
         * [FeedSectionType.LATEST] 의 시간 윈도우.
         *
         * 2026-08-06 이전에는 [FeedSectionType.NEAR] 에도 적용했으나, "가까운 스토리"는
         * 이름이 거리만 약속하므로 기간 필터를 뗐다(해당 enum KDoc 참고).
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
         * [FeedSectionType.NEAR] 의 반경 상한. 도보권을 조금 넘는 5km.
         *
         * 피드 전체에서 유일한 반경 필터다. 리포지토리 SQL 에는 `ST_DWithin` 이 없고
         * KNN 정렬만 하므로(밀도가 낮아도 후보가 비지 않게), 거리 제한은 여기서만 건다.
         *
         * **이 값을 못 채우면 섹션을 내리지 않는다.** 예전에는 반경을 풀고 헤더만
         * 바꿔 내보냈는데, 저밀도 지역에서 100km 밖 글이 "가까운"이라는 이름을 달고
         * 나왔다. 빈 섹션이 거짓 섹션보다 낫다. (2026-08-06)
         */
        private const val NEAR_RADIUS_METERS = 5_000.0

        private const val FEED_CACHE_TTL_SECONDS = 60L
    }
}
