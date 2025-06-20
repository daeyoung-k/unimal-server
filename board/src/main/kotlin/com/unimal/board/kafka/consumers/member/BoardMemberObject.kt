package com.unimal.board.kafka.consumers.member

import com.unimal.board.domain.member.BoardUser
import com.unimal.board.domain.member.BoardUserRepository
import com.unimal.common.dto.kafka.SignInUser
import org.springframework.stereotype.Component

@Component
class BoardMemberObject(
    private val boardUserRepository: BoardUserRepository
) {

    fun boardUserRegister(signInUser: SignInUser) {
        BoardUser(
            email = signInUser.email,
            name = signInUser.name,
            nickname = signInUser.nickname,
            profileImageUrl = signInUser.profileImageUrl,
            withdrawalAt = signInUser.withdrawalAt,
        ).let { boardUser ->
            boardUserRepository.save(boardUser)
        }
    }
}