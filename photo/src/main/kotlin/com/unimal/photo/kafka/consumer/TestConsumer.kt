package com.unimal.photo.kafka.consumer

import com.unimal.photo.service.PhotoService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TestConsumer(
    private val photoService: PhotoService
) {

    @KafkaListener(topics = ["test.topic"], groupId = "unimal-photo-group")
    fun testConsumer(message: String) {
        println("컨슈머 카프카 데이터 응답 $message")
        photoService.kafkaTest(message)
    }
}