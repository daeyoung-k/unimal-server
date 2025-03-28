package com.unimal.user.service.login.dto

data class MemberInfo(
    val provider: String,
    val email: String,
    var nickname: String? = null,
    var name: String? = null,
    var tel: String? = null
)
