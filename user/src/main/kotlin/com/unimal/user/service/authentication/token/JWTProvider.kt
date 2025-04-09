package com.unimal.user.service.authentication.token

import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JWTProvider {

    @Value("\${custom.jwt.secret-key}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    fun createAccessToken() {
        val test: String = this.secretKey + "test"
        println(test)
        println(this.key)
    }

    fun createRefreshToken() {

    }

    fun getClaims() {

    }
}