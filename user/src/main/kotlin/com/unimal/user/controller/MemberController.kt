package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.config.annotation.UserInfoAnnotation
import com.unimal.user.service.MemberService
import org.springframework.web.bind.annotation.GetMapping
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


}


