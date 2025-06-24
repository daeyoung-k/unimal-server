package com.unimal.user.service.login.enums

enum class LoginType {
    MANUAL, KAKAO, NAVER, GOOGLE, TEST
    ;
    companion object {
        fun from(value: String?): LoginType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MANUAL
        }
    }
}