package com.unimal.user.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.controller.request.*
import com.unimal.user.service.authentication.AuthenticationService
import com.unimal.user.service.login.LoginService
import com.unimal.user.service.member.MemberService
import com.unimal.user.service.token.TokenService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val loginService: LoginService,
    private val tokenService: TokenService,
    private val authenticationService: AuthenticationService,
    private val memberService: MemberService
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

    @PostMapping("/signup/manual")
    fun manualSignup(
        @RequestBody @Valid signupRequest: SignupRequest,
    ): CommonResponse {
        loginService.signup(signupRequest)
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

    @PostMapping("/email/duplicated-check")
    fun emailDuplicatedCheck(
        @RequestBody @Valid emailRequest: EmailRequest,
    ): CommonResponse {
        memberService.getDuplicatedEmailCheck(emailRequest)
        return CommonResponse()
    }

    @PostMapping("/email/code-request")
    fun emailCodeRequest(
        @RequestBody @Valid emailRequest: EmailRequest,
    ): CommonResponse {
        authenticationService.sendMailAuthCodeRequest(emailRequest)
        return CommonResponse()
    }

    @PostMapping("/email/code-verify")
    fun emailCodeVerify(
        @RequestBody @Valid emailAuthCodeVerifyRequest: EmailAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.emailAuthCodeVerify(emailAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-request")
    fun telCodeRequest(
        @RequestBody @Valid telRequest: TelRequest
    ): CommonResponse {
        authenticationService.sendTelAuthCodeRequest(telRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-verify")
    fun telCodeVerify(
        @RequestBody @Valid telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.telAuthCodeVerify(telAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-request")
    fun emailTelCodeRequest(
        @RequestBody @Valid emailTelAuthCodeRequest: EmailTelAuthCodeRequest
    ): CommonResponse {
        authenticationService.sendEmailTelAuthCodeRequest(emailTelAuthCodeRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-verify")
    fun emailTelCodeVerify(
        @RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/check-update")
    fun telCheckUpdate(
        @RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest,
        response: HttpServletResponse
    ): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        val jwtToken = loginService.telCheckUpdate(emailTelAuthCodeVerifyRequest.email, emailTelAuthCodeVerifyRequest.tel)
        response.setHeader("X-Unimal-Email", jwtToken.email)
        response.setHeader("X-Unimal-Access-Token", jwtToken.accessToken)
        response.setHeader("X-Unimal-Refresh-Token", jwtToken.refreshToken)
        response.setHeader("X-Unimal-Provider", jwtToken.provider)
        return CommonResponse()
    }
}