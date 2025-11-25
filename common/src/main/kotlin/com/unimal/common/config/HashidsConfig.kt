package com.unimal.common.config

import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HashidsConfig(
    @Value("\${hashids.salt}")
    private val salt: String,
    @Value("\${hashids.min-length}")
    private val minLength: Int
) {

    @Bean
    fun hashids(): Hashids {
        return Hashids(salt, minLength)
    }
}