package com.unimal.photo.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PhotoKafkaTestRepository: JpaRepository<PhotoKafkaTest, Long> {
    // Define any custom query methods if needed
}