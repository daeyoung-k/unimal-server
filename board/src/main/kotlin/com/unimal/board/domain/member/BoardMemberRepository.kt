package com.unimal.board.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface BoardMemberRepository: JpaRepository<BoardMember, Long> {

    fun findByEmail(email: String): BoardMember?
}