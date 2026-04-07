package com.unimal.user.kafka.topics

import com.unimal.common.dto.kafka.user.SignInUser
import com.unimal.common.dto.kafka.user.UpdateUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MemberKafkaTopic(
    private val kafkaUserTemplate: KafkaTemplate<String, SignInUser>,
    private val kafkaUserUpdateTemplate: KafkaTemplate<String, UpdateUser>,
    private val kafkaStringTemplate: KafkaTemplate<String, String>
) {
    val logger = KotlinLogging.logger {}

    fun signInTopicIssue(signInUser: SignInUser) {
        try {
            kafkaUserTemplate.send("user.signInTopic", signInUser)
        } catch (e: Exception) {
            logger.error(e) { "회원가입 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun userUpdateTopicIssue(updateUser: UpdateUser) {
        try {
            kafkaUserUpdateTemplate.send("user.userUpdateTopic", updateUser)
        } catch (e: Exception) {
            logger.error(e) { "회원 업데이트 토픽 발행 오류 : ${e.message}" }
        }
    }

    fun reSignInTopicIssue(email: String) {
        try {
            kafkaStringTemplate.send("user.reSignInTopic", email)
        } catch (e: Exception) {
            logger.error(e) { "재가입 토픽 발행 오류 : ${e.message}" }
        }
    }
}