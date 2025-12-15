package com.unimal.board.service.post.manager

import com.unimal.board.domain.board.reply.BoardReReplyRepository
import com.unimal.board.domain.board.reply.BoardReply
import com.unimal.board.domain.board.reply.BoardReplyRepository
import org.springframework.stereotype.Component

@Component
class ReplyManager(
    private val boardReplyRepository: BoardReplyRepository,
    private val boardReReplyRepository: BoardReReplyRepository
) {

    fun saveReply(
        boardReply: BoardReply
    ) = boardReplyRepository.save(boardReply)
}