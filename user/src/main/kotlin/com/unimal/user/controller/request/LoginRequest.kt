package com.unimal.user.controller.request

import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType

sealed interface LoginRequest {
    val provider: LoginType
}

data class KakaoLoginRequest(
    override val provider: LoginType = LoginType.KAKAO,
    val token: String
) : LoginRequest

data class NaverLoginRequest(
    override val provider: LoginType = LoginType.NAVER,
    val email: String,
    val name: String?,
    val nickname: String?,
    val profileImage: String?,
) : LoginRequest {
    fun toUserInfo(): UserInfo {
        return UserInfo(
            provider = provider.name,
            email = email,
            name = name,
            nickname = nickname,
            profileImage = profileImage
        )
    }
}

data class GoogleLoginRequest(
    override val provider: LoginType = LoginType.GOOGLE,
    val email: String,
    val name: String?,
    val nickname: String?,
    val profileImage: String?,
) : LoginRequest {
    fun toUserInfo(): UserInfo {
        return UserInfo(
            provider = provider.name,
            email = email,
            name = name,
            nickname = nickname,
            profileImage = profileImage
        )
    }
}

data class ManualLoginRequest(
    override val provider: LoginType = LoginType.MANUAL,
    val email: String,
    val password: String,
) : LoginRequest {
    fun toUserInfo(): UserInfo {
        return UserInfo(
            provider = provider.name,
            email = email,
            password = password
        )
    }
}