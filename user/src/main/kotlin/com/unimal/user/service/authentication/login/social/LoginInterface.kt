package com.unimal.user.service.authentication.login.social

import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType

interface LoginInterface {
    fun provider(): LoginType
    fun <T> getInfo(info: T): UserInfo
}