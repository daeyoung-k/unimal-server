package com.unimal.user.service.authentication.login

import com.unimal.user.domain.member.Member
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType

interface LoginInterface {
    fun provider(): LoginType
    fun <T> getUserInfo(info: T): UserInfo
    fun getMember(userInfo: UserInfo): Member
}