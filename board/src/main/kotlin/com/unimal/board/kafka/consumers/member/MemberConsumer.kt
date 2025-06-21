package com.unimal.board.kafka.consumers.member

import com.unimal.common.dto.kafka.SignInUser
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MemberConsumer(
    private val boardMemberObject: BoardMemberObject
) {

    @KafkaListener(topics = ["user.signInTopic"], groupId = "unimal-board-group")
    fun signInConsumer(signInUser: SignInUser) {
        boardMemberObject.boardUserSignIn(signInUser)
    }

    @KafkaListener(topics = ["user.withdrawalTopic"], groupId = "unimal-board-group")
    fun withdrawalConsumer(email: String) {
        boardMemberObject.withdrawal(email)
    }

    @KafkaListener(topics = ["user.reSignInTopic"], groupId = "unimal-board-group")
    fun reSignInConsumer(email: String) {
        boardMemberObject.reSignIn(email)
    }
}