package com.unimal.user.service.login.dto

import com.unimal.user.domain.member.Member

data class UserInfo(
    val provider: String,
    val email: String,
    var nickname: String? = null,
    var name: String? = null,
    var tel: String? = null
) {
    fun toEntity(): Member {
        return Member(
            email = email,
            provider = provider,
            nickname = nickname,
            name = name,
            tel = tel
        )
    }
}
