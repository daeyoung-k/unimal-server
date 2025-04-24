package com.unimal.user.domain.authentication

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "authentication_token", schema = "unimal-auth")
open class AuthenticationToken(

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "refresh_token", unique = true, length = 1000)
    val refreshToken: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null,

) : BaseIdEntity()