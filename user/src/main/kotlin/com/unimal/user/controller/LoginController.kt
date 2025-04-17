package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.service.authentication.login.LoginService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class LoginController(
    private val loginService: LoginService
) {

    /**
     * Handles mobile login requests via Kakao by processing the provided social login token.
     *
     * @param token The Kakao social login token extracted using the custom annotation.
     * @return A generic response indicating the result of the login attempt.
     */
    @GetMapping("/mobile/kakao")
    fun mobileKakao(
        @SocialLoginToken token: String
    ): CommonResponse {
        loginService.login(KakaoLoginRequest(token = token))
        return CommonResponse()
    }
}