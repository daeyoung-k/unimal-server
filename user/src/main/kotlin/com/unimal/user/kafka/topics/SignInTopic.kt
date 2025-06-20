package com.unimal.user.kafka.topics

import com.unimal.common.dto.kafka.SignInUser
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class SignInTopic(
    private val kafkaTemplate: KafkaTemplate<String, SignInUser>
) {
    fun signInTopicIssue(signInUser: SignInUser) {
        kafkaTemplate.send("user:sign-in-topic", signInUser)
    }
}