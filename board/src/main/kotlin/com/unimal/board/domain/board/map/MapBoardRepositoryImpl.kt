package com.unimal.board.domain.board.map

import com.unimal.board.service.post.dto.map.MapPostInfo
import com.unimal.board.utils.HashidsUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository

@Repository
class MapBoardRepositoryImpl(
    @PersistenceContext private val em: EntityManager,
    private val hashidsUtil: HashidsUtil
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
                b.id, b.title, b.content, b.street_name,
                ST_Y(b.location::geometry) AS latitude,
                ST_X(b.location::geometry) AS longitude,
                b.created_at,
                bf.file_url, COUNT(DISTINCT bl.id) as like_count, COUNT(DISTINCT br.id) as reply_count
            FROM board b
            INNER JOIN LATERAL (
                SELECT bf.file_url
                FROM board_file bf
                WHERE bf.board_id = b.id
                ORDER BY bf.id ASC
                LIMIT 1
            ) bf ON true
            LEFT  JOIN board_like  bl ON bl.board_id = b.id
            LEFT  JOIN board_reply br ON br.board_id = b.id AND br.del = false
            WHERE ST_DWithin(b.location, ST_MakePoint(:lng, :lat)::geography, :radius)
              AND b.del = false
              AND b.show = 'PUBLIC'
            GROUP BY b.id, bf.file_url
            ORDER BY (
              CASE WHEN b.email = :userEmail THEN 10000.0 ELSE 0.0 END
              + CASE
                  WHEN b.created_at >= NOW() - INTERVAL '30 minutes' THEN 1.0
                  WHEN b.created_at >= NOW() - INTERVAL '2 hours'    THEN 0.6
                  WHEN b.created_at >= NOW() - INTERVAL '6 hours'    THEN 0.3
                  ELSE 0.1
                END
              + COUNT(DISTINCT bl.id) * 2.0
              + COUNT(DISTINCT br.id) * 3.0
            ) DESC
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
                    id          = hashidsUtil.encode((row[0] as Number).toLong()),
                    title       = row[1] as? String,
                    content     = row[2] as String,
                    streetName  = row[3] as? String,
                    latitude    = (row[4] as Number).toDouble(),
                    longitude   = (row[5] as Number).toDouble(),
                    createdAt   = (row[6] as java.sql.Timestamp).toLocalDateTime(),
                    fileUrl     = row[7] as? String,
                    likeCount   = (row[8] as Number).toLong(),
                    replyCount  = (row[9] as Number).toLong(),
                )
            }
    }
}
