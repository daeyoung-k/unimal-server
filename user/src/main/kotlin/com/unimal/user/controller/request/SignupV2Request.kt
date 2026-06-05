package com.unimal.user.controller.request

import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

data class SignupV2Request(
    @field:NotBlank
    val nickname: String,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val checkPassword: String,
    @field:NotBlank
    val password: String,
) {
    fun toUserInfo() = UserInfo(
        email = email,
        password = BCryptPasswordEncoder().encode(password.lowercase()),
        nickname = nickname,
        provider = LoginType.MANUAL.name
    )
}
