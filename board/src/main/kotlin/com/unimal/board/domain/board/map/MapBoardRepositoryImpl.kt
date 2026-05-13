package com.unimal.board.domain.board.map

import com.unimal.board.service.post.dto.map.MapPostInfo
import com.unimal.board.utils.HashidsUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class MapBoardRepositoryImpl(
    @PersistenceContext private val em: EntityManager,
) {

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
                  CASE WHEN b.email = :userEmail THEN 10000.0 ELSE 0.0 END
                  + CASE
                      WHEN b.created_at >= NOW() - INTERVAL '30 minutes' THEN 1.0
                      WHEN b.created_at >= NOW() - INTERVAL '2 hours'    THEN 0.6
                      WHEN b.created_at >= NOW() - INTERVAL '6 hours'    THEN 0.3
                      ELSE 0.1
                    END
                  + COALESCE(bl.like_count, 0) * 2.0
                  + COALESCE(br.reply_count, 0) * 3.0
                ) AS score,
                (CASE WHEN b.email = :userEmail Then 'T' ELSE '' END) as is_owner
            FROM board b
            INNER JOIN LATERAL (
                SELECT bf.file_url
                FROM board_file bf
                WHERE bf.board_id = b.id
                ORDER BY bf.id ASC
                LIMIT 1
            ) bf ON true
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
            WHERE ST_DWithin(b.location, ST_MakePoint(:lng, :lat)::geography, :radius)
              AND b.del = false
              AND (
                    (b.map_show = 'SAME' AND b.show = 'PUBLIC')
                    OR b.map_show = 'PUBLIC'
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
                    title           = row[3]?.toString(),
                    content         = row[4].toString(),
                    streetName      = row[5]?.toString(),
                    latitude        = (row[6] as Number).toDouble(),
                    longitude       = (row[7] as Number).toDouble(),
                    createdAt       = (row[8] as Timestamp).toLocalDateTime(),
                    likeCount       = (row[9] as Number).toLong(),
                    replyCount      = (row[10] as Number).toLong(),
                    score           = (row[11] as Number).toDouble(),
                    isOwner         = row[12]?.toString() == "T"
                )
            }
    }
}
