package com.unimal.board.utils

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheManager(
    private val stringRedisTemplate: StringRedisTemplate,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun getCache(key: String): String? = stringRedisTemplate.opsForValue().get(key)

    fun getMultiCache(
        keys: List<String>
    ): MutableList<String>? {
        return stringRedisTemplate.opsForValue().multiGet(keys)
    }

    fun getStringCacheSet(key: String): Set<String> = stringRedisTemplate.opsForSet().members(key) ?: emptySet()

    fun setStringCache(key: String, value: String) {
        stringRedisTemplate.opsForValue().set(key, value)
    }

    fun setAnyCache(key: String, value: Any) {
        redisTemplate.opsForValue().set(key, value)
    }

    fun setStringCacheSeconds(key: String, value: String, duration: Long) =
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration))

    fun setStringCacheMinutes(key: String, value: String, duration: Long) =
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofMinutes(duration))

    fun setAnyCacheSeconds(key: String, value: Any, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration))

    fun setAnyCacheMinutes(key: String, value: String, duration: Long) =
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(duration))

    fun addStringCache(key: String, values: Collection<String>) {
        stringRedisTemplate.opsForSet().add(key, *values.toTypedArray())
    }

    fun deleteCache(key: String) {
        stringRedisTemplate.delete(key)
    }
}