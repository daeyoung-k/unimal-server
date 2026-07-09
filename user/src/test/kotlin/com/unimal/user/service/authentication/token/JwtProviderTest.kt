package com.unimal.user.service.authentication.token

import com.unimal.common.enums.TokenType
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.JwtProvider
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Base64
import java.util.Date

class JwtProviderTest {

    private val secretKey = "and0LXNlY3JldC1rZXktand0LXNlY3JldC1rZXktand0LXNlY3JldC1rZXk="
    private val provider = JwtProvider().apply {
        this.secretKey = this@JwtProviderTest.secretKey
    }

    @Test
    fun `Base64 인코딩`() {
        val testJwtSecretKey = "jwt-secret-key-jwt-secret-key-jwt-secret-key".toByteArray()
        val base64 = Base64.getEncoder().encodeToString(testJwtSecretKey)
        assertNotNull(base64)
        println("Base64: $base64")
    }

    @Test
    fun `JWT 액세스 토큰 발급하기`() {
        val email = "test@test.com"
        val nickname = "테스트"
        val role = listOf("ROLE_USER", "ROLE_ADMIN")
        provider.createAccessToken(email, nickname, LoginType.TEST, role).let {
            assertNotNull(it)
            println("Access Token: $it")
        }
    }

    @Test
    fun `JWT 리프레쉬 토큰 발급하기`() {
        val email = "test@test.com"
        val nickname = "테스트"
        val role = listOf("ROLE_USER", "ROLE_ADMIN")
        provider.createRefreshToken(email, nickname, LoginType.TEST, role).let {
            assertNotNull(it)
            println("Refresh Token: $it")
        }
    }

    @Test
    fun `JWT 토큰 인증`() {
        val activeToken = provider.createRefreshToken(
            email = "test@test.com",
            nickname = "테스트",
            provider = LoginType.TEST,
            roles = listOf("ROLE_USER", "ROLE_ADMIN")
        )
        provider.getClaims(activeToken).let {
            assertNotNull(it)
            println("Claims: $it")
        }
    }

    @Test
    fun `JWT 기간 만료`() {
        val failToken = Jwts.builder()
            .expiration(Date(System.currentTimeMillis() - 1000L))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
            .claim("type", TokenType.REFRESH.name)
            .subject("test@test.com")
            .compact()
        assertThrows(ExpiredJwtException::class.java) {provider.getClaims(failToken)}
    }


}
