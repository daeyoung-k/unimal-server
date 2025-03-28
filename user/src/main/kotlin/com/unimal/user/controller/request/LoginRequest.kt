package com.unimal.user.controller.request

import com.unimal.user.service.login.LoginType

interface LoginRequest {
    val provider: LoginType
}

data class KakaoLoginRequest(
    override val provider: LoginType = LoginType.KAKAO,
    val token: String
) : LoginRequest

data class NaverLoginRequest(
    override val provider: LoginType = LoginType.NAVER,
    val token: String
) : LoginRequest

data class GoogleLoginRequest(
    override val provider: LoginType = LoginType.KAKAO,
    val token: String
) : LoginRequest

data class ManualLoginRequest(
    override val provider: LoginType = LoginType.MANUAL,
    val email: String,
    val password: String,
    val name: String,
    val tel: String
) : LoginRequest