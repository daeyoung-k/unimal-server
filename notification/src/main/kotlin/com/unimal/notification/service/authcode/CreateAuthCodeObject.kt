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
        redisTemplate.opsForValue().set(key, randomCode, Duration.ofMinutes(5))
        return randomCode
    }

    fun createMailTelAuthCode(email: String, tel: String): String {
        val key = "$email:$tel:auth-code"
        val randomCode = random()
        redisTemplate.opsForValue().set(key, randomCode, Duration.ofMinutes(5))
        return randomCode
    }

    fun createTelAuthCode(tel: String): String {
        val key = "$tel:auth-code"
        val randomCode = random()
        redisTemplate.opsForValue().set(key, randomCode, Duration.ofMinutes(5))
        return randomCode
    }

    private fun random() = (100000..999999).random().toString()
}