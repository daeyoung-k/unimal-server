package com.unimal.board.domain.board.like

import org.springframework.data.jpa.repository.JpaRepository

interface BoardLikeRepository: JpaRepository<BoardLike, Long> {
}