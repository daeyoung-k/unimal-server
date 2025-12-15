package com.unimal.board.domain.board.reply

import org.springframework.data.jpa.repository.JpaRepository

interface BoardReplyRepository: JpaRepository<BoardReply, Long> {
}