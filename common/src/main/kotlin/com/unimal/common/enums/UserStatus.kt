package com.unimal.common.enums

enum class UserStatus(
    val description: String,
) {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    BLOCK("차단"),
    WITHDRAWAL("탈퇴"),
    RESIGNIN("재가입 요청")
}