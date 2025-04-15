package com.unimal.user.service.authentication.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTProvider {
    @Value("\${custom.jwt.secret-key}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)) }
    private val accessExpiration: Long = 1000L * 60 * 60 // 1hour
    private val refreshExpiration: Long = 1000L * 60 * 60 * 24 * 180 // 180days

    fun createAccessToken(
        email: String,
        role: List<String>
    ): String {
        return Jwts.builder()
            .expiration(Date(System.currentTimeMillis() + accessExpiration))
            .signWith(key)
            .claim("type", "access")
            .claim("role", role)
            .subject(email)
            .compact()
    }

    fun createRefreshToken(
        email: String,
        role: List<String>
    ): String {
        return Jwts.builder()
            .expiration(Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key)
            .claim("type", "refresh")
            .claim("role", role)
            .subject(email)
            .compact()
    }

    fun getClaims(
        token: String
    ): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}