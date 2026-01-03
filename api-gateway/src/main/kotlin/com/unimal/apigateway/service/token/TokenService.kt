package com.unimal.apigateway.service.token

import com.unimal.apigateway.exception.CustomException
import com.unimal.apigateway.exception.TokenNotFoundException
import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
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
        val tokenType = TokenType.from(claims["type"] as String)
        val email = claims.subject

        return when (tokenType) {
            TokenType.ACCESS -> {
                tokenManager.getCacheToken(email, token) ?: throw TokenNotFoundException("토큰이 만료 되었습니다.")
                CommonUserInfo(
                    email = email,
                    roles = claims["roles"] as List<String>,
                    provider = claims["provider"] as String,
                    tokenType = TokenType.ACCESS,
                    nickname = claims["nickname"] as String
                )
            }
            TokenType.REFRESH -> {
                val refreshToken = tokenManager.getDbToken(email, token)
                if (refreshToken == null || refreshToken.revoked || LocalDateTime.now() > refreshToken.issuedAt.plusDays(180)
                    ) throw TokenNotFoundException("토큰이 만료 되었습니다.")

                CommonUserInfo(
                    email = email,
                    roles = claims["roles"] as List<String>,
                    provider = claims["provider"] as String,
                    tokenType = TokenType.REFRESH,
                    nickname = claims["nickname"] as String
                )
            }
            else -> {
                logger.error { "지원하지 않는 타입의 토큰입니다. : $tokenType" }
                throw CustomException("지원하지 않는 타입의 토큰입니다.")
            }
        }

    }
}