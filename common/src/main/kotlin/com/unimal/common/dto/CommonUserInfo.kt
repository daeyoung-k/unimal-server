package com.unimal.common.dto

import com.unimal.common.enums.TokenType

data class CommonUserInfo(
    val email: String,
    val nickname: String,
    val roles: List<String>,
    val provider: String,
    val tokenType: TokenType
) {
    val roleString: String
        get() = roles.joinToString(",")
}
