package com.unimal.board.domain.board.like

import com.unimal.board.domain.board.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardLikeRepository: JpaRepository<BoardLike, Long> {

    fun findByBoardAndEmail(board: Board, email: String): BoardLike?

    @Query("""
        select count(bl) from BoardLike bl where bl.board = :board
    """)
    fun countByBoard(board: Board): Int
}