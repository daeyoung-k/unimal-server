package com.unimal.user.service.authentication.token

import io.jsonwebtoken.ExpiredJwtException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.Base64

@SpringBootTest
@ActiveProfiles("local")
class JWTProviderTest {

    @Autowired
    private val provider = JWTProvider()

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
        val role = listOf("ROLE_USER", "ROLE_ADMIN")
        provider.createAccessToken(email, role).let {
            assertNotNull(it)
            println("Access Token: $it")
        }
    }

    @Test
    fun `JWT 리프레쉬 토큰 발급하기`() {
        val email = "test@test.com"
        val role = listOf("ROLE_USER", "ROLE_ADMIN")
        provider.createRefreshToken(email, role).let {
            assertNotNull(it)
            println("Refresh Token: $it")
        }
    }

    @Test
    fun `JWT 토큰 인증`() {
        val activeToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NjAyNzQ3NzYsInR5cGUiOiJyZWZyZXNoIiwicm9sZSI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sInN1YiI6InRlc3RAdGVzdC5jb20ifQ.PeKeh_86YsVajeOMee6QnljvLcp4pp4Cgu8b2OtYkOw"
        provider.getClaims(activeToken).let {
            assertNotNull(it)
            println("Claims: $it")
        }
    }

    @Test
    fun `JWT 기간 만료`() {
        val failToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NDQ3MjIzNzIsInR5cGUiOiJyZWZyZXNoIiwicm9sZSI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sInN1YiI6InRlc3RAdGVzdC5jb20ifQ.XQZ8JsmaUuGaebV5_JyjfyMyF3BmodZoeFfUvMMuswg"
        assertThrows(ExpiredJwtException::class.java) {provider.getClaims(failToken)}
    }


}