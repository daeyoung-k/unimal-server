package com.unimal.user.service.authentication.login

import com.unimal.user.service.authentication.login.enums.LoginType

fun interface Login {
    fun provider(): LoginType
}