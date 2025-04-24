package com.unimal.user.service.authentication.token

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TokenManager(
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun saveToken(
        email: String,
        token: String,
    ) {
        redisTemplate.opsForValue().set("$email:access-token", token, Duration.ofMinutes(60))
    }

    fun getToken(
        email: String,
    ): String? {
        return redisTemplate.opsForValue().get("$email:access-token")
    }

}