package com.unimal.board.kafka.consumers.member

import com.unimal.common.dto.kafka.SignInUser
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SignInConsumer(
    private val boardMemberObject: BoardMemberObject
) {

    @KafkaListener(topics = ["user:sign-in-topic"], groupId = "unimal-board-group")
    fun signInConsumer(signInUser: SignInUser) {
        println("컨슈머 카프카 데이터 응답 ${signInUser.email} ")
        boardMemberObject.boardUserRegister(signInUser)
    }
}