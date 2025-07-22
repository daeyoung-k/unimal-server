package com.unimal.user.service.token

import com.unimal.user.domain.authentication.AuthenticationToken
import com.unimal.user.domain.authentication.AuthenticationTokenRepository
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.utils.RedisCacheManager
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TokenManager(
    private val redisCacheManager: RedisCacheManager,
    private val jwtProvider: JwtProvider,
    private val authenticationTokenRepository: AuthenticationTokenRepository
) {

    fun getCacheToken(
        email: String,
    ): String? {
        return redisCacheManager.getCache("$email:access-token")
    }

    fun saveCacheToken(
        email: String,
        token: String,
    ) {
        redisCacheManager.setCacheMinutes("$email:access-token", token,60)
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
        redisCacheManager.deleteCache("$email:access-token")
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

    fun deleteDbToken(
        email: String
    ) {
        val refreshToken = getDbToken(email)
        if (refreshToken == null) return
        authenticationTokenRepository.delete(refreshToken)
    }

    fun createJwtToken(
        email: String,
        provider: LoginType,
        role: List<String>,
    ): JwtTokenDTO {
        val accessToken = jwtProvider.createAccessToken(
            email = email,
            provider = provider,
            roles = role
        )
        val refreshToken = jwtProvider.createRefreshToken(
            email = email,
            provider = provider,
            roles = role
        )
        saveCacheToken(email, accessToken)
        upsertDbToken(email, refreshToken)
        return JwtTokenDTO(
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken,
            provider = provider.name,
        )
    }

}