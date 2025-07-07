package com.unimal.user.service.login

import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.user.utils.RedisCacheManager
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import org.springframework.stereotype.Component

@Component("ManualLoginObject")
class ManualLoginObject(
    private val memberObject: MemberObject,
    private val redisCacheManager: RedisCacheManager
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
        val member =  memberObject.getEmailMember(userInfo.email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        val psCheck = memberObject.passwordCheck(userInfo.password!!, member.password!!)
        return if (psCheck) {
            member
        } else {
            throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
        }

    }

    fun emailTelSuccessCheck(email: String, tel: String): Boolean {
        val emailKey = "$email:auth-code"
        val telKey = "$email:$tel:auth-code"
        val emailCheck = redisCacheManager.getCache(emailKey)
        val telCheck = redisCacheManager.getCache(telKey)
        return emailCheck == "SUCCESS" && telCheck == "SUCCESS"
    }
}