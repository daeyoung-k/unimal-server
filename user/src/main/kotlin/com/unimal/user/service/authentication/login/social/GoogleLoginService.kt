package com.unimal.user.service.authentication.login.social

import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import org.springframework.stereotype.Component

@Component("googleLogin")
class GoogleLoginService: LoginInterface {
    override fun provider(): LoginType = LoginType.GOOGLE
    override fun <T> getInfo(info: T): UserInfo {
        TODO("Not yet implemented")
    }
}