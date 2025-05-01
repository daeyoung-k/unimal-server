package com.unimal.apigateway.domain.authentication

import org.springframework.data.jpa.repository.JpaRepository

interface AuthenticationTokenRepository: JpaRepository<AuthenticationToken, Long> {
    fun findByEmailAndRefreshToken(email: String, refreshToken: String): AuthenticationToken?
}