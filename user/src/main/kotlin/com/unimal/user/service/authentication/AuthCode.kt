package com.unimal.user.service.authentication

enum class AuthCode(
    val description: String,
    val code: String
) {
    MASTER("모든 인증을 통과하는 마스터 코드", "260912")
}