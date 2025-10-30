package com.unimal.board.service.member

import com.unimal.board.domain.member.BoardMember
import com.unimal.board.domain.member.BoardMemberRepository
import org.springframework.stereotype.Component

@Component
class MemberManager(
    private val boardMemberRepository: BoardMemberRepository
) {

    fun findByEmail(
        email: String
    ) = boardMemberRepository.findByEmail(email)

    fun saveMember(boardMember: BoardMember) = boardMemberRepository.save(boardMember)
}