package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.controller.request.EmailAuthCodeVerifyRequest
import com.unimal.user.controller.request.EmailRequest
import com.unimal.user.controller.request.EmailTelAuthCodeRequest
import com.unimal.user.controller.request.EmailTelAuthCodeVerifyRequest
import com.unimal.user.controller.request.TelAuthCodeVerifyRequest
import com.unimal.user.controller.request.TelRequest
import com.unimal.user.service.authentication.AuthenticationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/email/code-request")
    fun emailCodeRequest(@RequestBody @Valid emailRequest: EmailRequest): CommonResponse {
        authenticationService.sendMailAuthCodeRequest(emailRequest)
        return CommonResponse()
    }

    @PostMapping("/email/code-verify")
    fun emailCodeVerify(@RequestBody @Valid emailAuthCodeVerifyRequest: EmailAuthCodeVerifyRequest): CommonResponse {
        authenticationService.emailAuthCodeVerify(emailAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-request")
    fun telCodeRequest(@RequestBody @Valid telRequest: TelRequest): CommonResponse {
        authenticationService.sendTelAuthCodeRequest(telRequest)
        return CommonResponse()
    }

    @PostMapping("/tel/code-verify")
    fun telCodeVerify(@RequestBody @Valid telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest): CommonResponse {
        authenticationService.telAuthCodeVerify(telAuthCodeVerifyRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-request")
    fun emailTelCodeRequest(@RequestBody @Valid emailTelAuthCodeRequest: EmailTelAuthCodeRequest): CommonResponse {
        authenticationService.sendEmailTelAuthCodeRequest(emailTelAuthCodeRequest)
        return CommonResponse()
    }

    @PostMapping("/email-tel/code-verify")
    fun emailTelCodeVerify(@RequestBody @Valid emailTelAuthCodeVerifyRequest: EmailTelAuthCodeVerifyRequest): CommonResponse {
        authenticationService.emailTelAuthCodeVerify(emailTelAuthCodeVerifyRequest)
        return CommonResponse()
    }
}
