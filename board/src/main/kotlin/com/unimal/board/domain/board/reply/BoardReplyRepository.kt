package com.unimal.board.domain.board.reply

import com.unimal.board.domain.board.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardReplyRepository: JpaRepository<BoardReply, Long> {

    @Query("""
        select count(br) from BoardReply br where br.board = :board
    """)
    fun countByBoard(board: Board): Int

    @Query("""
        select rereply.* from board_reply rereply left join board_reply reply on rereply.reply_id = reply.id
        where rereply.board_id = :boardId
            order by
                coalesce(reply.created_at, rereply.created_at) desc,
                case when reply.reply_id is null then 0 else 1 end asc,
                rereply.created_at asc;
    """, nativeQuery = true)
    fun getBoardReplyByBoardId(boardId: Long): List<BoardReply>

}