package com.unimal.user.service.token.dto

data class JwtTokenDTO(
    val accessToken: String,
    val refreshToken: String
)
