package com.unimal.user.service.login.naver

import com.unimal.user.service.login.Login
import com.unimal.user.service.login.LoginType
import org.springframework.stereotype.Component

@Component("naverLogin")
class NaverLoginService: Login {
    override fun provider(): LoginType = LoginType.NAVER

}