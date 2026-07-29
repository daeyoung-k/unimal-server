package com.unimal.board.domain.board.map

import com.unimal.board.service.post.dto.map.MapPostInfo
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class MapBoardRepositoryImpl(
    @PersistenceContext private val em: EntityManager,
) {

    /**
     * 지도 마커 조회 — 반경 내 글을 score 내림차순으로 [postLimit] 건까지.
     *
     * score = 신선도 + 좋아요×2 + 댓글×3 + 사진 보유 3
     *
     * **본인글 보너스(+10000)는 제거됐다 (2026-07-29).** 내 글이 어디서나 최상위가
     * 되어 마커 크기 위계·캡션 우선권·스택/클러스터 대표를 독점했다. 내 글 보기
     * 필터가 따로 있으므로 기본 탐색에서는 모두에게 공평한 노출 기회를 준다.
     *
     * 대신 신선도 배점을 1.0/0.6/0.3 → 20.0/8.0/3.0 으로 올렸다. 보너스만 빼면
     * 새 글 score 가 4점대로 떨어져 `ORDER BY score DESC LIMIT` 에서 밀리고,
     * 방금 올린 글이 지도에 안 보인다. 30분간 상위권에 올렸다가 자연 하락시킨다.
     *
     * [userEmail] 은 score 와 무관하며 is_owner(마커 링 색)·is_like 판정에만 쓴다.
     *
     * 설계: unimal-flutter `docs/specs/2026-07-28-마커-사진-우선-대표-선정.md`
     */
    fun findLocationPosts(
        userEmail: String,
        lat: Double,
        lng: Double,
        radiusMeters: Double,
        postLimit: Int,
    ): List<MapPostInfo> {
        val sql = """
            SELECT
                b.id, bm.nickname, bm.profile_image, b.title, b.content, b.street_name,
                ST_Y(b.location::geometry) AS latitude,
                ST_X(b.location::geometry) AS longitude,
                b.created_at,
                COALESCE(bl.like_count, 0) AS like_count,
                COALESCE(br.reply_count, 0) AS reply_count,
                (
                  CASE
                      WHEN b.created_at >= NOW() - INTERVAL '30 minutes' THEN 20.0
                      WHEN b.created_at >= NOW() - INTERVAL '2 hours'    THEN 8.0
                      WHEN b.created_at >= NOW() - INTERVAL '6 hours'    THEN 3.0
                      ELSE 0.1
                    END
                  + COALESCE(bl.like_count, 0) * 2.0
                  + COALESCE(br.reply_count, 0) * 3.0
                  + CASE WHEN bf.board_id IS NOT NULL THEN 3.0 ELSE 0.0 END
                ) AS score,
                (CASE WHEN b.email = :userEmail Then 'T' ELSE '' END) as is_owner,
                EXISTS (
                    SELECT 1
                    FROM board_like my_bl
                    WHERE my_bl.board_id = b.id
                      AND my_bl.email = :userEmail
                ) AS is_like
            FROM board b
            LEFT JOIN board_member bm on bm.email = b.email
            LEFT JOIN (
                SELECT board_id, COUNT(*) AS like_count
                FROM board_like
                GROUP BY board_id
            ) bl ON bl.board_id = b.id
            LEFT JOIN (
                SELECT board_id, COUNT(*) AS reply_count
                FROM board_reply
                WHERE del = false
                GROUP BY board_id
            ) br ON br.board_id = b.id
            LEFT JOIN (
                SELECT DISTINCT board_id
                FROM board_file
            ) bf ON bf.board_id = b.id
            WHERE ST_DWithin(b.location, ST_MakePoint(:lng, :lat)::geography, :radius)
              AND b.del = false
              AND b.show = 'PUBLIC'
              AND (
                    bf.board_id IS NOT NULL
                    OR b.created_at >= NOW() - INTERVAL '48 hours'
                )    
            ORDER BY score DESC
            LIMIT :limit
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        return (em.createNativeQuery(sql)
            .setParameter("userEmail", userEmail)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
            .setParameter("radius", radiusMeters)
            .setParameter("limit", postLimit)
            .resultList as List<Array<Any?>>)
            .map { row ->
                MapPostInfo(
                    id              = row[0].toString(),
                    nickname        = row[1]?.toString() ?: "",
                    profileImage    = row[2]?.toString(),
                    title           = row[3]?.toString() ?: "",
                    content         = row[4].toString(),
                    streetName      = row[5]?.toString(),
                    latitude        = (row[6] as Number).toDouble(),
                    longitude       = (row[7] as Number).toDouble(),
                    createdAt       = (row[8] as Timestamp).toLocalDateTime(),
                    likeCount       = (row[9] as Number).toLong(),
                    replyCount      = (row[10] as Number).toLong(),
                    score           = (row[11] as Number).toDouble(),
                    isOwner         = row[12]?.toString() == "T",
                    isLike         = row[13] as Boolean,
                )
            }
    }
}
