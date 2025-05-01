package com.unimal.common.dto

data class CommonUserInfo(
    val email: String,
    val roles: List<String>,
    val provider: String,
) {
    val roleString: String
        get() = roles.joinToString(",")
}
