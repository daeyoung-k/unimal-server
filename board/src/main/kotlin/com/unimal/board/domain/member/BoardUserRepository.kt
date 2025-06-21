package com.unimal.board.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface BoardUserRepository: JpaRepository<BoardUser, Long> {

    fun findByEmail(email: String): BoardUser?
}