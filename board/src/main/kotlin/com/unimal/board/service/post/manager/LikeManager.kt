package com.unimal.board.service.post.manager

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.like.BoardLike
import com.unimal.board.domain.board.like.BoardLikeRepository
import com.unimal.board.utils.RedisCacheManager
import org.springframework.stereotype.Component

@Component
class LikeManager(
    private val boardLikeRepository: BoardLikeRepository,
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

    fun existingLike(
        board: Board,
        email: String
    ) = boardLikeRepository.findByBoardAndEmail(board, email)

    fun deleteBoardLike(boardLike: BoardLike) = boardLikeRepository.delete(boardLike)

    fun saveBoardLike(boardLike: BoardLike) = boardLikeRepository.save(boardLike)

    fun saveCachePostLikeGetCount(
        board: Board,
    ): Long {
        val key = "board_like:${board.id}"
        val count = boardLikeRepository.countByBoard(board)
        redisCacheManager.setAnyCache(key, count)
        return redisCacheManager.getCache(key)!!.toLong()
    }
}