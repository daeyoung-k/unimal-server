package com.unimal.user.service.member.dto

import com.unimal.user.service.login.enums.LoginType

data class FindEmailInfo(
    val email: String? = null,
    val loginType: LoginType? = null,
    val message: String? = null
)
