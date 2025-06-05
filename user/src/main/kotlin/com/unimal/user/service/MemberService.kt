package com.unimal.user.service

import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.member.dto.MemberInfo
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberObject: MemberObject
) {

    fun getMemberInfo(commonUserInfo: CommonUserInfo): MemberInfo {
        return memberObject.getMemberInfo(commonUserInfo.email, LoginType.from(commonUserInfo.provider))
    }
}