package com.unimal.board.domain.board

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardFileRepository: JpaRepository<BoardFile, Long> {

    @Query("""
        select bf.fileUrl from BoardFile bf
            where bf.board = :board 
            order by bf.main desc, bf.id asc
    """)
    fun findFileUrlsByBoardOrderByMainDescIdAsc(board: Board): List<String>
}