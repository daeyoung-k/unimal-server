package com.unimal.board.kafka.topics

import com.unimal.board.kafka.topics.dto.UserCountIssueType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class PostKafkaTopic(
    private val countKafkaTemplate: KafkaTemplate<String, UserCountIssueType>
) {
    val logger = KotlinLogging.logger {}

    fun likeCountCalculateEvent(userCountIssueType: UserCountIssueType) {
        try {
            countKafkaTemplate.send("board.likeCountCalculateTopic", userCountIssueType)
        } catch (e: Exception) {
            logger.error(e) { "유저 토탈 좋아요 카운트 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun postCountCalculateEvent(userCountIssueType: UserCountIssueType) {
        try {
            countKafkaTemplate.send("board.postCountCalculateTopic", userCountIssueType)
        } catch (e: Exception) {
            logger.error(e) { "유저 토탈 게시글 카운트 토픽 발행 오류 : ${e.message}" }
        }
    }
}