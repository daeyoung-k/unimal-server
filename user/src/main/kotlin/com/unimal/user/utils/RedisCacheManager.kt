package com.unimal.user.utils

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheManager(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun getCache(key: String): String? = redisTemplate.opsForValue().get(key)
    fun getCacheSet(key: String): Set<String> = redisTemplate.opsForSet().members(key) ?: emptySet()

    fun setCache(key: String, value: String) {
        redisTemplate.opsForValue().set(key, value)
    }

    fun setCacheSeconds(key: String, value: String, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration))

    fun setCacheMinutes(key: String, value: String, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(duration))

    fun addCache(key: String, value: String) {
        redisTemplate.opsForSet().add(key, value)
    }

    fun deleteCache(key: String) {
        redisTemplate.delete(key)
    }
}