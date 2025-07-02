package com.unimal.apigateway.service.token

import com.unimal.apigateway.exception.CustomException
import com.unimal.apigateway.exception.TokenNotFoundException
import com.unimal.common.dto.CommonUserInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TokenService(
    private val tokenManager: TokenManager
) {
    private val logger = KotlinLogging.logger {}

    fun getUserInfo(
        claims: Claims,
        token: String
        ): CommonUserInfo {
        val type = claims["type"] as String
        val email = claims.subject

        return when (type) {
            "access" -> {
                tokenManager.getCacheToken(email, token) ?: throw TokenNotFoundException("토큰이 만료 되었습니다.")
                CommonUserInfo(
                    email = email,
                    roles = claims["roles"] as List<String>,
                    provider = claims["provider"] as String,
                    tokenType = type
                )
            }
            "refresh" -> {
                val refreshToken = tokenManager.getDbToken(email, token)
                if (refreshToken == null || refreshToken.revoked || LocalDateTime.now() > refreshToken.issuedAt.plusDays(180)
                    ) throw TokenNotFoundException("토큰이 만료 되었습니다.")

                CommonUserInfo(
                    email = email,
                    roles = claims["roles"] as List<String>,
                    provider = claims["provider"] as String,
                    tokenType = type
                )
            }
            else -> {
                logger.error { "지원하지 않는 타입의 토큰입니다. : $type" }
                throw CustomException("지원하지 않는 타입의 토큰입니다.")
            }
        }

    }
}