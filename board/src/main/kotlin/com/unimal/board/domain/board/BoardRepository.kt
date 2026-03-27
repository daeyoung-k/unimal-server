package com.unimal.board.domain.board

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardRepository: JpaRepository<Board, Long> {
    fun findBoardById(id: Long): Board?

    @Query("""
        select b from Board b where b.id = :id and b.email.email = :email
    """)
    fun getBoardByIdAndEmail(id: Long, email: String): Board?

    @Query("""
        select count(b) from Board b where b.email.email = :email and b.del != true
    """)
    fun getUserTotalPostCount(email: String): Long
}