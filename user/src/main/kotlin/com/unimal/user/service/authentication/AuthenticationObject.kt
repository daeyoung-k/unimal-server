package com.unimal.user.service.authentication

import com.unimal.webcommon.exception.AuthCodeException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class AuthenticationObject(
    private val redisTemplate: RedisTemplate<String, String>,
) {

    fun getAuthCode(key: String): String {
        return redisTemplate.opsForValue().get(key) ?: throw AuthCodeException("인증코드가 존재하지 않습니다.")
    }

    fun setAuthCodeSuccess(key: String) {
        redisTemplate.opsForValue().set(key, "SUCCESS", Duration.ofMinutes(10))
    }
}