package com.unimal.user.controller

import com.unimal.user.service.login.LoginService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class LoginController(
    private val LoginService: LoginService
) {

    @GetMapping("/oauth2/kakao")
    fun oauth2Kakao(
        @RequestHeader header: HttpHeaders,
        request: HttpServletRequest
    ): String {
        val kakaoToken = header["Authorization"]
        LoginService.kakaoLogin(kakaoToken?.get(0)!!)
        return "oauth2 kakao"
    }
}