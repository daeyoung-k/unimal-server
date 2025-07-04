package com.unimal.user.service.token

import com.unimal.webcommon.exception.TokenException
import com.unimal.user.service.login.enums.LoginType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtProvider {

    val logger = KotlinLogging.logger {}

    @Value("\${custom.jwt.secret-key}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)) }
    private val accessExpiration: Long = 1000L * 60 * 60 // 1hour
    private val refreshExpiration: Long = 1000L * 60 * 60 * 24 * 180 // 180days

    fun createAccessToken(
        email: String,
        provider: LoginType,
        roles: List<String>
    ): String {
        return Jwts.builder()
            .expiration(Date(System.currentTimeMillis() + accessExpiration))
            .signWith(key)
            .claim("type", "access")
            .claim("provider", provider.name)
            .claim("roles", roles)
            .subject(email)
            .compact()
    }

    fun createRefreshToken(
        email: String,
        provider: LoginType,
        roles: List<String>
    ): String {
        return Jwts.builder()
            .expiration(Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key)
            .claim("type", "refresh")
            .claim("provider", provider.name)
            .claim("roles", roles)
            .subject(email)
            .compact()
    }

    fun getClaims(
        token: String
    ): Claims {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw e
        } catch (e: SignatureException) {
            throw e
        } catch (e: MalformedJwtException) {
            throw e
        } catch (e: Exception) {
            logger.error { "토큰 인증 실패: ${e.message}" }
            throw TokenException("토큰 인증 실패", 401, HttpStatus.UNAUTHORIZED)
        }
    }
}