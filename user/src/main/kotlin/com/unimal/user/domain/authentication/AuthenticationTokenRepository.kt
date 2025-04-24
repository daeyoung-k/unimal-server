package com.unimal.user.domain.authentication

import org.springframework.data.jpa.repository.JpaRepository

interface AuthenticationTokenRepository : JpaRepository<AuthenticationToken, Long> {

    fun findByEmail(
        email: String
    ): AuthenticationToken?
}