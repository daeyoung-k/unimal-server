package com.unimal.apigateway.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTProvider {

    val logger = KotlinLogging.logger {}

    @Value("\${custom.jwt.secret-key}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)) }

    fun tokenValidation(
        token: String
    ) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
        } catch (e: ExpiredJwtException) {
            logger.error { "토큰 인증 실패1: ${e.message}" }
            throw e
        } catch (e: SignatureException) {
            logger.error { "토큰 인증 실패2: ${e.message}" }
            throw e
        } catch (e: MalformedJwtException) {
            logger.error { "토큰 인증 실패3: ${e.message}" }
            throw e
        } catch (e: Exception) {
            logger.error { "토큰 인증 실패: ${e.message}" }
            throw e
        }
    }
}