package com.unimal.user.service.login.google

import com.unimal.user.service.login.Login
import com.unimal.user.service.login.LoginType
import org.springframework.stereotype.Component

@Component("googleLogin")
class GoogleLoginService: Login {
    override fun provider(): LoginType = LoginType.GOOGLE
}