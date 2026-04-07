package com.unimal.board.kafka.consumers.post

import com.unimal.board.kafka.topics.dto.UserCountIssue
import com.unimal.board.service.post.PostCalculateService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PostConsumer(
    private val postCalculateService: PostCalculateService
) {

    @KafkaListener(topics = ["board.likeCountCalculateTopic"], groupId = "unimal-board-group")
    fun likeCountCalculateConsumer(userCountIssue: UserCountIssue) {
        postCalculateService.likeCountCalculate(userCountIssue)
    }

    @KafkaListener(topics = ["board.postCountCalculateTopic"], groupId = "unimal-board-group")
    fun postCountCalculateConsumer(userCountIssue: UserCountIssue) {
        postCalculateService.postCountCalculate(userCountIssue)
    }

}