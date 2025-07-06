package com.unimal.user.service.token.dto

data class JwtTokenDTO(
    val email: String,
    val accessToken: String,
    val refreshToken: String
)
