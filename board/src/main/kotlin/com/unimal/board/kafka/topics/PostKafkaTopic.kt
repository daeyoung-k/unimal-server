package com.unimal.board.kafka.topics

import com.unimal.board.kafka.topics.dto.UserCountIssue
import com.unimal.common.dto.kafka.post.PostAppPushEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class PostKafkaTopic(
    private val countKafkaTemplate: KafkaTemplate<String, UserCountIssue>,
    private val appPushKafkaTemplate: KafkaTemplate<String, PostAppPushEvent>
) {
    val logger = KotlinLogging.logger {}

    fun likeCountCalculateEvent(userCountIssue: UserCountIssue) {
        try {
            countKafkaTemplate.send("board.likeCountCalculateTopic", userCountIssue)
        } catch (e: Exception) {
            logger.error { "UserCountIssue - $userCountIssue" }
            logger.error(e) { "유저 토탈 좋아요 카운트 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun postCountCalculateEvent(userCountIssue: UserCountIssue) {
        try {
            countKafkaTemplate.send("board.postCountCalculateTopic", userCountIssue)
        } catch (e: Exception) {
            logger.error { "UserCountIssue - $userCountIssue" }
            logger.error(e) { "유저 토탈 게시글 카운트 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun postAppPushEvent(postAppPushEvent: PostAppPushEvent) {
        try {
            appPushKafkaTemplate.send("board.postAppPushTopic", postAppPushEvent)
        } catch (e: Exception) {
            logger.error { "PostAppPushEvent - $postAppPushEvent" }
            logger.error(e) { "게시글 앱푸시 토픽 발행 오류 : ${e.message}" }
        }
    }
}