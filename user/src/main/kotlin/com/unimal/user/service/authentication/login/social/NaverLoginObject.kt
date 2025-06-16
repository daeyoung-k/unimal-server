package com.unimal.user.service.authentication.login.social

import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import org.springframework.stereotype.Component

@Component("NaverLoginObject")
class NaverLoginObject: LoginInterface {
    override fun provider(): LoginType = LoginType.NAVER
    override fun <T> getInfo(info: T): UserInfo {
        return if (info is NaverLoginRequest) {
            info.toUserInfo()
        } else {
            throw LoginException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

}