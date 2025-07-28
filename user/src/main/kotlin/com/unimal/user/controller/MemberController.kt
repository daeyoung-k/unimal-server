package com.unimal.user.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.controller.request.*
import com.unimal.user.service.authentication.AuthenticationService
import com.unimal.user.service.member.MemberService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/member")
class MemberController(
    private val memberService: MemberService,
    private val authenticationService: AuthenticationService
) {

    @GetMapping("/info")
    fun getInfo(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo
    ): CommonResponse {
        return CommonResponse(data = memberService.getMemberInfo(commonUserInfo))
    }

    @PatchMapping("/info/update")
    fun updateInfo(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo,
        @RequestBody @Valid infoUpdateRequest: InfoUpdateRequest
    ): CommonResponse {
        memberService.updateMemberInfo(commonUserInfo, infoUpdateRequest)
        return CommonResponse()
    }

    @PostMapping("/change/password")
    fun changePassword(
        @UserInfoAnnotation commonUserInfo: CommonUserInfo,
        @RequestBody @Valid changePasswordRequest: ChangePasswordRequest,
    ): CommonResponse {

        changePasswordRequest.email = commonUserInfo.email
        memberService.changePassword(changePasswordRequest)
        return CommonResponse()
    }



    @PostMapping("/find/email")
    fun findEmail(
        @RequestBody @Valid telAuthCodeVerifyRequest: TelAuthCodeVerifyRequest
    ): CommonResponse {
        authenticationService.telAuthCodeVerify(telAuthCodeVerifyRequest)
        return CommonResponse(data = memberService.findEmailByTel(telAuthCodeVerifyRequest.tel))
    }

    @PostMapping("/find/email-tel/check/request")
    fun findEmailTelCheckRequest(
        @RequestBody @Valid emailTelAuthCodeRequest: EmailTelAuthCodeRequest
    ): CommonResponse {
        memberService.checkEmailByTel(emailTelAuthCodeRequest.email, emailTelAuthCodeRequest.tel)
        authenticationService.sendEmailTelAuthCodeRequest(emailTelAuthCodeRequest)
        return CommonResponse()
    }

    @PostMapping("/find/change/password")
    fun findChangePassword(
        @RequestBody @Valid verifyChangePasswordRequest: VerifyChangePasswordRequest,
    ): CommonResponse {
        memberService.changePassword(verifyChangePasswordRequest)
        return CommonResponse()
    }

    @GetMapping("/find/nickname/duplicate")
    fun findNicknameDuplicate(
        @RequestParam(value = "nickname", required = true) nickname: String,
    ): CommonResponse {
        memberService.findNicknameDuplicate(nickname)
        return CommonResponse()
    }

    @PostMapping("/find/email/duplicate")
    fun emailDuplicatedCheck(
        @RequestBody @Valid emailRequest: EmailRequest,
    ): CommonResponse {
        memberService.getDuplicatedEmailCheck(emailRequest)
        authenticationService.sendMailAuthCodeRequest(emailRequest)
        return CommonResponse()
    }

    @PostMapping("/find/tel/duplicate")
    fun telDuplicatedCheck(
        @RequestBody @Valid telRequest: TelRequest,
    ): CommonResponse {
        memberService.getDuplicatedTelCheck(telRequest)
        authenticationService.sendTelAuthCodeRequest(telRequest)
        return CommonResponse()
    }

}


