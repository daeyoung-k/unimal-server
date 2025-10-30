package com.unimal.board.kafka.consumers.member

import com.unimal.board.domain.member.BoardMember
import com.unimal.board.domain.member.BoardMemberRepository
import com.unimal.common.dto.kafka.SignInUser
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BoardMemberObject(
    private val boardMemberRepository: BoardMemberRepository
) {

    fun getBoardUser(email: String) = boardMemberRepository.findByEmail(email)

    fun boardUserSignIn(signInUser: SignInUser) {
        BoardMember(
            email = signInUser.email,
            name = signInUser.name,
            nickname = signInUser.nickname,
            profileImage = signInUser.profileImage,
            withdrawalAt = signInUser.withdrawalAt,
        ).let { boardUser ->
            boardMemberRepository.save(boardUser)
        }
    }

    fun withdrawal(email: String) {
        getBoardUser(email)?.let { boardUser ->
            boardUser.withdrawalAt = LocalDateTime.now()
            boardMemberRepository.save(boardUser)
        }
    }

    fun reSignIn(email: String) {
        getBoardUser(email)?.let { boardUser ->
            boardUser.withdrawalAt = null
            boardMemberRepository.save(boardUser)
        }

    }


}