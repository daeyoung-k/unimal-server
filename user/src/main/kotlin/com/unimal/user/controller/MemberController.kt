package com.unimal.user.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.controller.request.InfoUpdateRequest
import com.unimal.user.controller.request.TelRequest
import com.unimal.user.service.member.MemberService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/member")
class MemberController(
    private val memberService: MemberService
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

    @PostMapping("/find/email")
    fun findEmail(
        @RequestBody @Valid telRequest: TelRequest
    ): CommonResponse {
        return CommonResponse()
    }

}


