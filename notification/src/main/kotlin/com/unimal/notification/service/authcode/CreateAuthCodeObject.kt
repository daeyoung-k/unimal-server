package com.unimal.notification.service.authcode

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CreateAuthCodeObject(
    private val redisTemplate: RedisTemplate<String, String>,
) {

    fun createAuthCodeCacheSaved(key: String): String {
        val randomCode = random()
        redisTemplate.opsForValue().set(key, randomCode, Duration.ofMinutes(5))
        return randomCode
    }

    private fun random() = (100000..999999).random().toString()
}