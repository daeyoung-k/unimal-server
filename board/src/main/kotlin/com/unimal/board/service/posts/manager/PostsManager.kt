package com.unimal.board.service.posts.manager

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.BoardFileRepository
import com.unimal.board.domain.board.BoardRepository
import com.unimal.board.utils.RedisCacheManager
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component

@Component
class PostsManager(
    private val boardFileRepository: BoardFileRepository,
    private val boardRepository: BoardRepository,
    private val geometryFactory: GeometryFactory,
    private val redisCacheManager: RedisCacheManager,
) {

    fun savedBoard(
        board: Board
    ) = boardRepository.save(board)

    fun savedBoardFile(
        boardFile: BoardFile
    ) = boardFileRepository.save(boardFile)

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
        redisCacheManager.setAnyCache(likeKey, 0)
        redisCacheManager.setAnyCache(replyKey, 0)
    }

    fun getBoard(id: Long) = boardRepository.findBoardById(id)

    fun getBoardFilesUrls(board: Board) = boardFileRepository.findFileUrlsByBoardOrderByMainDescIdAsc(board)

    fun getPostLike(
        boardId: String
    ): Int {
        val key = "board_like:$boardId"
        return redisCacheManager.getCache(key)?.toInt() ?: run {
            redisCacheManager.setAnyCache(key, 0)
            0
        }
    }

    fun getPostReply(
        boardId: String
    ): Int {
        val key = "board_reply:$boardId"
        return redisCacheManager.getCache(key)?.toInt() ?: run {
            redisCacheManager.setAnyCache(key, 0)
            0
        }
    }
}