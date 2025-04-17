package com.unimal.user.service.authentication.token.dto

data class JwtTokenDTO(
    val accessToken: String,
    val refreshToken: String
)
