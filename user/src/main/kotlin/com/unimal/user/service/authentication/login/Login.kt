package com.unimal.user.service.authentication.login

import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType

interface Login {
    fun provider(): LoginType
    fun <T> getInfo(info: T): UserInfo
}