package com.unimal.apigateway.service.token


import com.unimal.apigateway.domain.authentication.AuthenticationToken
import com.unimal.apigateway.domain.authentication.AuthenticationTokenRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TokenManager(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authenticationTokenRepository: AuthenticationTokenRepository
) {

    fun getCacheToken(
        email: String,
    ): String? {
        return redisTemplate.opsForValue().get("$email:access-token")
    }

    fun getDbToken(
        email: String,
    ): AuthenticationToken? {
        return authenticationTokenRepository.findByEmail(email)
    }

}