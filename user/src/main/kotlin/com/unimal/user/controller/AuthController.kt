package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.config.annotation.UserInfoAnnotation
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.service.authentication.login.LoginService
import com.unimal.user.service.authentication.token.TokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val loginService: LoginService,
    private val tokenService: TokenService
) {
    @GetMapping("/login/mobile/kakao")
    fun mobileKakao(
        @SocialLoginToken token: String,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(KakaoLoginRequest(token = token))
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/login/mobile/naver")
    fun mobileNaver(
        response: HttpServletResponse
    ): CommonResponse {
        // Naver login implementation
        return CommonResponse()
    }

    @GetMapping("/token-reissue")
    fun tokenReissue(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = tokenService.accessTokenCreate(commonUserInfo)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/logout")
    fun logout(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        tokenService.logout(commonUserInfo)
        return CommonResponse()
    }
}