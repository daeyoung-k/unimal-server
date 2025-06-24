package com.unimal.user.controller.request

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val tel: String,
    val nickname: String? = null,
)
