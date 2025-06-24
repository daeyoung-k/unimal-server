package com.unimal.notification.service.authcode

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CreateAuthCodeObject(
    private val redisTemplate: RedisTemplate<String, String>,
) {

    fun createMailAuthCode(email: String): String {
        val key = "$email:auth-code"
        val randomCode = random()
        redisTemplate.opsForValue().set(key, randomCode, Duration.ofMinutes(10))
        return randomCode
    }

    private fun random() = (100000..999999).random().toString()
}