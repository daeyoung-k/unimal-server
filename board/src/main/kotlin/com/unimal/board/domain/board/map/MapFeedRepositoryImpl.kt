package com.unimal.board.domain.board.map

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Tuple
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * 지도 바텀카드 피드 후보 조회.
 *
 * 설계: `docs/specs/2026-07-29-지도-바텀카드-피드-api.md` §5
 *
 * [MapBoardRepositoryImpl] 을 고치지 않고 새 클래스를 만들었다. 마커 쿼리는 앱이 이미
 * 쓰고 있어서 회귀 위험을 지고 싶지 않다. LATERAL 개선을 피드에서 먼저 검증하고
 * 안정되면 마커 쿼리에 이식한다.
 */
@Repository
class MapFeedRepositoryImpl(
    @PersistenceContext private val em: EntityManager,
) {

    /**
     * 현재 위치에서 **가까운 순**으로 [limit] 건.
     *
     * ## 반경 필터가 없는 것이 핵심이다
     *
     * `ORDER BY location <-> point` (PostGIS KNN)만 쓴다. 반경 파라미터가 없으므로
     * - 밀도가 낮으면 자동으로 먼 글까지 채운다 → 피드가 절대 비지 않는다
     * - 밀도가 높아지면 자동으로 가까운 글만 상단에 온다 → 별도 조치 없이 "동네 피드"가 된다
     *
     * `Board.location` 은 `geography(Point, 4326)` 이고, PostGIS 2.2+ 에서 geography 의
     * `<->` 는 **인덱스 보조 정확 거리 정렬**이다. 따라서 `idx_board_location_gist` 를 타고
     * [limit] 건을 찾은 즉시 스캔을 멈춘다 — 전국이 대상이어도 전체 스캔이 아니다.
     *
     * 기대 플랜:
     * ```
     * Limit
     *   └ Nested Loop (LATERAL)
     *       └ Index Scan using idx_board_location_gist on board   ← Order By: location <-> ...
     * ```
     *
     * ## 인덱스 predicate 함의
     *
     * `WHERE del = false AND show = 'PUBLIC'` 은 GiST 부분 인덱스 predicate 와 문자 그대로
     * 일치한다. `show` 를 문자열로 비교해야 함의가 성립한다 (인덱스가
     * `(show)::text = 'PUBLIC'::text` 로 캐스팅되어 있다). `location IS NOT NULL` 추가는
     * 쿼리 조건이 인덱스보다 강한 것이라 안전하다.
     *
     * ## 2차 정렬 키를 SQL 에 넣지 않는다
     *
     * 같은 좌표에 글이 여럿이면 distance 가 같아 순서가 매 요청마다 뒤집힌다. 보통
     * `ORDER BY distance, id DESC` 로 푸는데 **그러면 `Incremental Sort` 노드가 끼어들어
     * KNN 조기 종료가 깨진다.** 최대 [limit] 행이므로 tie-break 는 서비스에서 한다.
     *
     * ## LATERAL 을 쓰는 이유
     *
     * [MapBoardRepositoryImpl] 은 `LEFT JOIN (SELECT ... GROUP BY board_id)` 파생 테이블이라
     * **반경과 무관하게 `board_like`/`board_reply`/`board_file` 전체를 매 요청 훑는다.**
     * LATERAL 은 상관 서브쿼리라 LIMIT 을 통과한 [limit] 행에 대해서만 실행된다.
     *
     * `board_file` LATERAL 은 `LEFT` 다 — **사진 없는 글도 남긴다.** 지금 밀도에서 사진
     * 필수(`INNER`)로 걸면 피드가 빌 수 있다. 밀도가 오르면 `LEFT` 만 떼면 된다.
     */
    fun findNearCandidates(
        lat: Double,
        lng: Double,
        limit: Int,
    ): List<MapFeedCandidate> {
        val sql = """
            SELECT
                b.id                AS id,
                b.title             AS title,
                b.content           AS content,
                b.street_name       AS street_name,
                b.dong              AS dong,
                ST_Y(b.location::geometry) AS latitude,
                ST_X(b.location::geometry) AS longitude,
                ST_Distance(b.location, ST_MakePoint(:lng, :lat)::geography) AS distance_meters,
                b.created_at        AS created_at,
                bm.nickname         AS nickname,
                bm.profile_image    AS profile_image,
                bf.thumb_url        AS thumb_url,
                bf.file_url         AS file_url,
                COALESCE(agg.like_count, 0)  AS like_count,
                COALESCE(agg.reply_count, 0) AS reply_count
            FROM board b
            -- LEFT 유지. board_member 는 Kafka 로 복제되는 read model 이라 이벤트 유실 시
            -- 행이 없을 수 있다. INNER 로 바꾸면 그 글이 조용히 사라진다.
            LEFT JOIN board_member bm ON bm.email = b.email
            -- 대표 이미지 1장. LEFT 라서 사진 없는 글도 남는다.
            LEFT JOIN LATERAL (
                SELECT f.thumb_url, f.file_url
                FROM board_file f
                WHERE f.board_id = b.id
                  AND f.file_url IS NOT NULL
                ORDER BY f.main DESC, f.id ASC
                LIMIT 1
            ) bf ON TRUE
            LEFT JOIN LATERAL (
                SELECT
                    (SELECT COUNT(*) FROM board_like  l WHERE l.board_id = b.id) AS like_count,
                    (SELECT COUNT(*) FROM board_reply r WHERE r.board_id = b.id
                                                          AND r.del = false)     AS reply_count
            ) agg ON TRUE
            WHERE b.del = false
              AND b.show = 'PUBLIC'
              AND b.location IS NOT NULL
            ORDER BY b.location <-> ST_MakePoint(:lng, :lat)::geography
            LIMIT :limit
        """.trimIndent()

        return em.createNativeQuery(sql, Tuple::class.java)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
            .setParameter("limit", limit)
            .resultList
            .map { it as Tuple }
            .map { t ->
                // 컬럼명 기반 매핑. row[0]/row[13] 인덱스 접근은 컬럼을 중간에 하나 추가하면
                // 그 아래 전부 밀려서 컴파일은 통과하고 런타임에 깨진다.
                MapFeedCandidate(
                    id             = (t.get("id") as Number).toLong(),
                    title          = t.get("title") as String?,
                    content        = t.get("content").toString(),
                    streetName     = t.get("street_name") as String?,
                    dong           = t.get("dong") as String?,
                    latitude       = (t.get("latitude") as Number).toDouble(),
                    longitude      = (t.get("longitude") as Number).toDouble(),
                    distanceMeters = (t.get("distance_meters") as Number).toDouble(),
                    createdAt      = (t.get("created_at") as Timestamp).toLocalDateTime(),
                    nickname       = t.get("nickname") as String?,
                    profileImage   = t.get("profile_image") as String?,
                    thumbUrl       = t.get("thumb_url") as String?,
                    fileUrl        = t.get("file_url") as String?,
                    likeCount      = (t.get("like_count") as Number).toLong(),
                    replyCount     = (t.get("reply_count") as Number).toLong(),
                )
            }
    }
}
