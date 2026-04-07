package com.unimal.board.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BoardMemberRepository: JpaRepository<BoardMember, Long> {

    fun findByEmail(email: String): BoardMember?

    @Query("""
        select bm from BoardMember bm where bm.email = (select br.email from BoardReply br where br.id = :replyId)
    """)
    fun findByFcmTokenByReply(replyId: Long): BoardMember?
}