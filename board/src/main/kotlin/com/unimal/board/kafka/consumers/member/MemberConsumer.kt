package com.unimal.board.kafka.consumers.member

import com.unimal.board.domain.member.BoardMember
import com.unimal.board.domain.member.BoardMemberRepository
import com.unimal.common.dto.kafka.user.SignInUser
import com.unimal.common.dto.kafka.user.UpdateUser
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MemberConsumer(
    private val boardMemberRepository: BoardMemberRepository
) {

    @Transactional
    @KafkaListener(topics = ["user.signInTopic"], groupId = "unimal-board-group")
    fun signInConsumer(signInUser: SignInUser) {
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

    @Transactional
    @KafkaListener(topics = ["user.userUpdateTopic"], groupId = "unimal-board-group")
    fun userUpdateConsumer(updateUser: UpdateUser) {
        boardMemberRepository.findByEmail(updateUser.email)?.let { user ->
            if (!updateUser.name.isNullOrBlank()) user.nameUpdate(updateUser.name!!)
            if (!updateUser.nickname.isNullOrBlank()) user.nicknameUpdate(updateUser.nickname!!)
            if (!updateUser.profileImage.isNullOrBlank()) user.profileImageUpdate(updateUser.profileImage)
            if (!updateUser.fcmToken.isNullOrBlank()) user.fcmTokenUpdate(updateUser.fcmToken!!)
            boardMemberRepository.save(user)
        }
    }

    @Transactional
    @KafkaListener(topics = ["user.withdrawalTopic"], groupId = "unimal-board-group")
    fun withdrawalConsumer(email: String) {
        boardMemberRepository.findByEmail(email)?.let { user ->
            user.withdrawal()
            boardMemberRepository.save(user)
        }
    }

    @Transactional
    @KafkaListener(topics = ["user.reSignInTopic"], groupId = "unimal-board-group")
    fun reSignInConsumer(updateUser: UpdateUser) {
        boardMemberRepository.findByEmail(updateUser.email)?.let { user ->
            user.reSignIn(
                updateUser.name,
                updateUser.nickname,
                updateUser.profileImage
            )
            boardMemberRepository.save(user)
        }
    }
}