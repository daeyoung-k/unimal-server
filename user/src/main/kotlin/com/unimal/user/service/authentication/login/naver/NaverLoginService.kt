package com.unimal.user.service.authentication.login.naver

import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.service.authentication.login.Login
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import org.springframework.stereotype.Component

@Component("NaverLogin")
class NaverLoginService: Login {
    override fun provider(): LoginType = LoginType.NAVER
    override fun <T> getInfo(info: T): UserInfo {
        if (info !is NaverLoginRequest) {
            throw IllegalArgumentException("Invalid type for NaverLoginService")
        }
        return info.toUserInfo()
    }

}