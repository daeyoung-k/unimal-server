package com.unimal.user.service.authentication.login

import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import org.springframework.stereotype.Component

@Component("ManualLoginObject")
class ManualLoginObject(
    private val memberObject: MemberObject
): LoginInterface {
    override fun provider(): LoginType = LoginType.MANUAL

    override fun <T> getUserInfo(info: T): UserInfo {
        return if (info is ManualLoginRequest) {
            info.toUserInfo()
        } else {
            throw LoginException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

    override fun getMember(userInfo: UserInfo): Member {
        val member =  memberObject.getMember(userInfo.email, provider()) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        val psCheck = memberObject.passwordCheck(userInfo.password!!, member.password!!)
        return if (psCheck) {
            member
        } else {
            throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
        }

    }
}