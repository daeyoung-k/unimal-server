package com.unimal.user.service.login

import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import org.springframework.stereotype.Component

@Component("NaverLoginObject")
class NaverLoginObject(
    private val memberObject: MemberObject
): LoginInterface {
    override fun provider(): LoginType = LoginType.NAVER
    override fun <T> getUserInfo(info: T): UserInfo {
        return if (info is NaverLoginRequest) {
            info.toUserInfo()
        } else {
            throw LoginException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

    override fun getMember(userInfo: UserInfo): Member {
        return memberObject.getMember(userInfo.email, provider()) ?: memberObject.signIn(userInfo)
    }

}