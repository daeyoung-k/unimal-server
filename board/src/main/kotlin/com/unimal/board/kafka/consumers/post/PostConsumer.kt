package com.unimal.board.kafka.consumers.post

import com.unimal.board.kafka.topics.dto.UserCountIssueType
import com.unimal.board.service.post.PostCalculateService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PostConsumer(
    private val postCalculateService: PostCalculateService
) {

    @KafkaListener(topics = ["board.likeCountCalculateTopic"], groupId = "unimal-board-group")
    fun likeCountCalculateConsumer(userCountIssueType: UserCountIssueType) {
        postCalculateService.likeCountCalculate(userCountIssueType)
    }

    @KafkaListener(topics = ["board.postCountCalculateTopic"], groupId = "unimal-board-group")
    fun postCountCalculateConsumer(userCountIssueType: UserCountIssueType) {
        postCalculateService.postCountCalculate(userCountIssueType)
    }

}