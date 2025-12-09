package com.unimal.apigateway.service.token


import com.unimal.apigateway.domain.authentication.AuthenticationToken
import com.unimal.apigateway.domain.authentication.AuthenticationTokenRepository
import com.unimal.common.enums.TokenType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TokenManager(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authenticationTokenRepository: AuthenticationTokenRepository
) {

    fun getCacheToken(
        email: String,
        token: String
    ): String? {
        val key = "$email:${TokenType.ACCESS.cacheKey}"
        val cacheToken = redisTemplate.opsForValue().get(key)
        return if (cacheToken == token) cacheToken else null
    }

    fun getDbToken(
        email: String,
        token: String
    ): AuthenticationToken? {
        return authenticationTokenRepository.findByEmailAndRefreshToken(email, token)
    }

}