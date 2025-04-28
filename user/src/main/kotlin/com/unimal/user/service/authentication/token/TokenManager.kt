package com.unimal.user.service.authentication.token

import com.unimal.user.domain.authentication.AuthenticationToken
import com.unimal.user.domain.authentication.AuthenticationTokenRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

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

    fun saveCacheToken(
        email: String,
        token: String,
    ) {
        redisTemplate.opsForValue().set("$email:access-token", token, Duration.ofMinutes(60))
    }

    fun upsertDbToken(
        email: String,
        token: String,
    ) {
        val refreshToken = getDbToken(email)
        if (refreshToken == null) {
            authenticationTokenRepository.save(
                AuthenticationToken(
                    email = email,
                    refreshToken = token,
                )
            )
        } else {
            if (refreshToken.refreshToken != token) {
                refreshToken.refreshToken = token
                refreshToken.issuedAt = LocalDateTime.now()
                refreshToken.updatedAt = LocalDateTime.now()
                refreshToken.revoked = false
                authenticationTokenRepository.save(refreshToken)
            }
        }
    }

    fun deleteCacheToken(
        email: String,
    ) {
        redisTemplate.delete("$email:access-token")
    }

    fun revokDbToken(
        email: String
    ) {
        val refreshToken = getDbToken(email)
        if (refreshToken != null) {
            refreshToken.revoked = true
            refreshToken.updatedAt = LocalDateTime.now()
            authenticationTokenRepository.save(refreshToken)
        }
    }

    private fun getDbToken(
        email: String,
    ): AuthenticationToken? {
        return authenticationTokenRepository.findByEmail(email)
    }


}