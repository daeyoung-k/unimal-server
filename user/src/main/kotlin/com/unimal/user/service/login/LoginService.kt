package com.unimal.user.service.login

import org.springframework.stereotype.Service

@Service
class LoginService(
    private val loginService: Login
) {

    fun kakaoLogin(token: String) {
        loginService.getInfo(token)
        println("kakao login")
    }
}