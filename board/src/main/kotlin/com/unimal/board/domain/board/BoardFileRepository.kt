package com.unimal.board.domain.board

import org.springframework.data.jpa.repository.JpaRepository

interface BoardFileRepository: JpaRepository<BoardFile, Long> {
}