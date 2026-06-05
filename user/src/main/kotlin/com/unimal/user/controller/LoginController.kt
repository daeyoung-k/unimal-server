package com.unimal.user.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.controller.request.EmailTelAuthCodeVerifyRequest
import com.unimal.user.controller.request.GoogleLoginRequest
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.service.authentication.AuthenticationService
import com.unimal.user.service.login.LoginService
import com.unimal.user.service.token.TokenService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class LoginController(
    private val loginService: LoginService,
    private val tokenService: TokenService,
    private val authenticationService: AuthenticationService,
) {
    @GetMapping("/login/mobile/kakao")
    fun mobileKakao(
        @SocialLoginToken token: String,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(KakaoLoginRequest(token = token))
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/mobile/naver")
    fun mobileNaver(
        @RequestBody @Valid naverLoginRequest: NaverLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(naverLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/mobile/google")
    fun mobileGoogle(
        @RequestBody @Valid googleLoginRequest: GoogleLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(googleLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @PostMapping("/login/manual")
    fun manualLogin(
        @RequestBody @Valid manualLoginRequest: ManualLoginRequest,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = loginService.login(manualLoginRequest)
        response.setHeader("X-Unimal-Email", jwtToken?.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken?.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken?.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/token-reissue")
    fun tokenReissue(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo,
        response: HttpServletResponse
    ): CommonResponse {
        val jwtToken = tokenService.accessTokenCreate(commonUserInfo)
        response.setHeader("X-Unimal-Email", jwtToken.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        return CommonResponse()
    }

    @GetMapping("/logout")
    fun logout(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        loginService.logout(commonUserInfo)
        return CommonResponse()
    }

    @GetMapping("/withdrawal")
    fun withdrawal(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        loginService.withdrawal(commonUserInfo)
        return CommonResponse()
    }

    @PostMapping("/tel/check-update")
    fun telCheckUpdate(
        @RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest,
        response: HttpServletResponse
    ): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        val jwtToken = loginService.telCheckUpdate(
            emailTelAuthCodeVerifyRequest.email,
            emailTelAuthCodeVerifyRequest.tel
        )
        response.setHeader("X-Unimal-Email", jwtToken.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        response.setHeader("X-Unimal-Provider", jwtToken.provider)
        return CommonResponse()
    }
}
