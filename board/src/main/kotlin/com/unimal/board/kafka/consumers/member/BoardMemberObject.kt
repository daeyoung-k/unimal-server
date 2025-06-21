package com.unimal.board.kafka.consumers.member

import com.unimal.board.domain.member.BoardUser
import com.unimal.board.domain.member.BoardUserRepository
import com.unimal.common.dto.kafka.SignInUser
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BoardMemberObject(
    private val boardUserRepository: BoardUserRepository
) {

    fun getBoardUser(email: String) = boardUserRepository.findByEmail(email)

    fun boardUserSignIn(signInUser: SignInUser) {
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

    fun withdrawal(email: String) {
        getBoardUser(email)?.let { boardUser ->
            boardUser.withdrawalAt = LocalDateTime.now()
            boardUserRepository.save(boardUser)
        }
    }

    fun reSignIn(email: String) {
        getBoardUser(email)?.let { boardUser ->
            boardUser.withdrawalAt = null
            boardUserRepository.save(boardUser)
        }

    }


}