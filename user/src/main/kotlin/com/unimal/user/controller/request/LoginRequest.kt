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

/**
 * Apple 로그인 요청.
 * 이메일은 클라이언트 값을 신뢰하지 않고 identityToken(JWT) 검증 결과에서 추출한다.
 * name/nickname은 애플이 "최초 인증 1회"에만 내려주므로 그때 반드시 저장해야 한다.
 * authorizationCode는 refresh_token 교환용이며, 탈퇴 시 revoke에 사용한다.
 */
data class AppleLoginRequest(
    override val provider: LoginType = LoginType.APPLE,
    val identityToken: String,
    val authorizationCode: String? = null,
    val name: String? = null,
    val nickname: String? = null,
) : LoginRequest

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