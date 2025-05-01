package com.unimal.user.domain.authentication

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "authentication_token")
open class AuthenticationToken(

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "refresh_token", unique = true, length = 1000)
    var refreshToken: String,

    var revoked: Boolean = false,

    var issuedAt: LocalDateTime = LocalDateTime.now(),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null,

    ) : BaseIdEntity()