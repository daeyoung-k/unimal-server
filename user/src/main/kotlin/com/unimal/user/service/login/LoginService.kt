package com.unimal.user.service.login

import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.LoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.exception.LoginException
import com.unimal.user.service.login.kakao.KakaoLogin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLogin") private val kakaoLoginService: Login
) {

    fun login(loginRequest: LoginRequest) {

        when (loginRequest) {
            is KakaoLoginRequest -> {
                kakaoLoginService as KakaoLogin
                val memberInfo = kakaoLoginService.getInfo(loginRequest.token)
            }
            is NaverLoginRequest -> {

            }
            else -> {
                throw LoginException("지원하지 않는 로그인 방식입니다.")
            }
        }

    }
}