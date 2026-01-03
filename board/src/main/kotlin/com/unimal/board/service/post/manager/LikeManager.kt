package com.unimal.board.service.post.manager

import com.unimal.board.domain.board.Board
import com.unimal.board.utils.RedisCacheManager
import org.springframework.stereotype.Component

@Component
class LikeManager(
    private val redisCacheManager: RedisCacheManager,
) {

    fun getCachePostLikeCount(
        boardId: String
    ): Long {
        val key = "board_like:$boardId"
        return redisCacheManager.getCache(key)?.toLong() ?: run {
            redisCacheManager.setAnyCache(key, 0L)
            0L
        }
    }

    fun saveCachePostLikeGetCount(
        board: Board,
        count: Int
    ): Long {
        val key = "board_like:${board.id}"
        redisCacheManager.setAnyCache(key, count)
        return redisCacheManager.getCache(key)!!.toLong()
    }
}