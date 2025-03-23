package com.unimal.user.service.login

import com.unimal.user.service.login.kakao.KakaoLogin
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val loginService: Login
) {

    fun kakaoLogin(token: String) {
        loginService as KakaoLogin

        loginService.getInfo(token)
        println("kakao login")
    }
}