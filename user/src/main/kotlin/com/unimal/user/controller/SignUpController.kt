package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.controller.request.SignupRequest
import com.unimal.user.controller.request.SignupV2Request
import com.unimal.user.service.login.LoginService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class SignUpController(
    private val loginService: LoginService
) {
    @PostMapping("/signup/manual")
    fun manualSignup(
        @RequestBody @Valid signupRequest: SignupRequest,
    ): CommonResponse {
        loginService.signup(signupRequest)
        return CommonResponse()
    }

    @PostMapping("/signup/v2/manual")
    fun manualSignupV2(
        @RequestBody @Valid signupRequest: SignupV2Request,
    ): CommonResponse {
        loginService.signupV2(signupRequest)
        return CommonResponse()
    }
}
