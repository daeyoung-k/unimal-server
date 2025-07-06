package com.unimal.user.utils

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheManager(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun getCache(key: String): String? = redisTemplate.opsForValue().get(key)

    fun setCacheSeconds(key: String, value: String, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration))

    fun setCacheMinutes(key: String, value: String, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(duration))

    fun deleteCache(key: String) {
        redisTemplate.delete(key)
    }
}