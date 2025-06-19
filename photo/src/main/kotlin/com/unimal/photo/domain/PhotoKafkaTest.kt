package com.unimal.photo.domain

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "photo_kafka_test")
open class PhotoKafkaTest(
    val message: String? = null,
) : BaseIdEntity()