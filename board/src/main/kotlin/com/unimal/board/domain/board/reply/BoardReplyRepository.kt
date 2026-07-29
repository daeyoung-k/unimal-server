package com.unimal.board.domain.board.reply

import com.unimal.board.domain.board.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardReplyRepository: JpaRepository<BoardReply, Long> {

    // coalesce(br.del, false) = false → br.del = false (2026-07-29).
    // coalesce 는 부분 인덱스 WHERE del = false 의 predicate 함의를 깨서 인덱스를 못 탄다.
    // del 을 NOT NULL 로 만들었으므로 coalesce 방어가 필요 없다.
    @Query("""
        select count(br) from BoardReply br where br.board = :board and br.del = false
    """)
    fun countByBoard(board: Board): Int

    @Query("""
        select 
            rereply.*,
            (select nickname from member where email = rereply.email) nickname,
            (select profile_image from member where email = rereply.email) profile_image
         from board_reply rereply left join board_reply reply on rereply.reply_id = reply.id
        where rereply.board_id = :boardId
            order by
                coalesce(reply.created_at, rereply.created_at) desc,
                case when reply.reply_id is null then 0 else 1 end asc,
                rereply.created_at asc;
    """, nativeQuery = true)
    fun getBoardReplyByBoardId(boardId: Long): List<BoardReplyListInterface>

    fun findByIdAndBoardAndEmail(id: Long, board: Board, email: String): BoardReply?

}