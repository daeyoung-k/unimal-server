package com.unimal.board.service.post.manager

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.reply.BoardReply
import com.unimal.board.domain.board.reply.BoardReplyRepository
import com.unimal.board.utils.RedisCacheManager
import org.springframework.stereotype.Component

@Component
class ReplyManager(
    private val boardReplyRepository: BoardReplyRepository,

    private val redisCacheManager: RedisCacheManager,
) {
    fun getBoardReplyIdAndBoardAndEmail(
        replyId: Long,
        board: Board,
        email: String
    ) = boardReplyRepository.findByIdAndBoardAndEmail(replyId, board, email)

    fun saveReply(
        boardReply: BoardReply
    ) = boardReplyRepository.save(boardReply)

    fun saveCachePostReplyCount(
        board: Board
    ) {
        val key = "board_reply:${board.id}"
        val count = boardReplyRepository.countByBoard(board)
        redisCacheManager.setAnyCache(key, count)
    }

    fun getCachePostReplyCount(
        boardId: String
    ): Long {
        val key = "board_reply:$boardId"
        return redisCacheManager.getCache(key)?.toLong() ?: run {
            redisCacheManager.setAnyCache(key, 0L)
            0L
        }
    }

    fun getBoardReplyList(boardId: Long) = boardReplyRepository.getBoardReplyByBoardId(boardId)

}