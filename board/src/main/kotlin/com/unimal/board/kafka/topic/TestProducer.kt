package com.unimal.board.kafka.topic

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class TestProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    fun testTopic(message: String) {
        kafkaTemplate.send("test.topic", message)
    }

}