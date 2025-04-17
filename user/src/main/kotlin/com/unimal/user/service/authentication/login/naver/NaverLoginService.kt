package com.unimal.user.service.authentication.login.naver

import com.unimal.user.service.authentication.login.Login
import com.unimal.user.service.authentication.login.enums.LoginType
import org.springframework.stereotype.Component

@Component("naverLogin")
class NaverLoginService: Login {
    override fun provider(): LoginType = LoginType.NAVER

}