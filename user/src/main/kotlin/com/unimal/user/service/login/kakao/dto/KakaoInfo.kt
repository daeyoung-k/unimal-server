package com.unimal.user.service.login.kakao.dto

data class KakaoInfo(
    val kakao_account: KakaoAccount
)

data class KakaoAccount(
    val email: String,
    val profile: KakaoProfile
)

data class KakaoProfile(
    val nickname: String
)
