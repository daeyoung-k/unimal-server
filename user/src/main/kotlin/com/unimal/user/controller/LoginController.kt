package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.service.authentication.login.LoginService
import com.unimal.user.service.authentication.token.TokenManager
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class LoginController(
    private val loginService: LoginService,
    private val tokenManager: TokenManager
) {

    @GetMapping("/mobile/kakao")
    fun mobileKakao(
        @SocialLoginToken token: String,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(KakaoLoginRequest(token = token))
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/test")
    fun test() {
        val token = tokenManager.getToken("a5678936@hanmail.net")
        println(token)
    }
}