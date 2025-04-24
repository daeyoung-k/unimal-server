package com.unimal.common.dto

data class UserInfo(
    val email: String,
    val roles: List<String>,
    val provider: String,
)
