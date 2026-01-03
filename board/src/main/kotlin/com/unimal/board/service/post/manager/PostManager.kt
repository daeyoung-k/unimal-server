package com.unimal.board.service.post.manager

import com.unimal.board.utils.RedisCacheManager
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component

@Component
class PostManager(
    private val geometryFactory: GeometryFactory,
    private val redisCacheManager: RedisCacheManager,
) {

    fun createLocationPointInfo(
        longitude: Double?,
        latitude: Double?
    ): Point? {
        return if (longitude != null && latitude != null) {
            geometryFactory.createPoint(
                Coordinate(longitude, latitude)
            )
        } else null
    }

    fun createCachePostLikeAndReplyCount(
        boardId: String
    ) {
        val likeKey = "board_like:$boardId"
        val replyKey = "board_reply:$boardId"
        redisCacheManager.setAnyCache(likeKey, 0L)
        redisCacheManager.setAnyCache(replyKey, 0L)
    }

    fun postOwnerCheck(
        userEmail: String,
        boardEmail: String
    ) = userEmail.trim().equals(boardEmail.trim(), ignoreCase = true)
}