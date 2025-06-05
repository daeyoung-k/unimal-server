package com.unimal.user.service.member.dto

data class MemberInfo(
    val email: String,
    val provider: String,
    val name: String? = null,
    val nickName: String? = null,
    val tel: String? = null,
    val introduction: String? = null,
    val birthday: String? = null,
    val gender: String? = null,
)
