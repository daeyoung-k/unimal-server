package com.unimal.user.kafka.topics

import com.unimal.common.dto.kafka.SignInUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MemberKafkaTopic(
    private val kafkaUserTemplate: KafkaTemplate<String, SignInUser>,
    private val kafkaStringTemplate: KafkaTemplate<String, String>
) {
    val logger = KotlinLogging.logger {}

    fun signInTopicIssue(signInUser: SignInUser) {
        try {
            kafkaUserTemplate.send("user.signInTopic", signInUser)
        } catch (e: Exception) {
            logger.error { "회원가입 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun withdrawalTopicIssue(email: String) {
        try {
            kafkaStringTemplate.send("user.withdrawalTopic", email)
        } catch (e: Exception) {
            logger.error { "회원 탈퇴 토픽 발행 오류 : ${e.message}" }
            logger.error { "회원 탈퇴 토픽 발행 오류 : $e" }
        }
    }

    fun reSignInTopicIssue(email: String) {
        try {
            kafkaStringTemplate.send("user.reSignInTopic", email)
        } catch (e: Exception) {
            logger.error { "재가입 토픽 발행 오류 : ${e.message}" }
        }
    }
}