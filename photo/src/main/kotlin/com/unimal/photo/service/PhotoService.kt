package com.unimal.photo.service

import com.unimal.photo.domain.PhotoKafkaTest
import com.unimal.photo.domain.PhotoKafkaTestRepository
import org.springframework.stereotype.Service

@Service
class PhotoService(
    private val photoKafkaTestRepository: PhotoKafkaTestRepository
) {

    fun kafkaTest(message: String) {
        println("서비스 카프카 데이터 응답 $message")
        photoKafkaTestRepository.save(
            PhotoKafkaTest(message = message)
        )
    }
}