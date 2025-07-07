package com.unimal.user.controller.request

import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

data class SignupRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val checkPassword: String,
    @field:NotBlank
    val password: String,
    @field:NotBlank
    val tel: String,

    val nickname: String? = null,

    val provider: LoginType = LoginType.MANUAL,
) {
    fun toUserInfo() = UserInfo(
        name = name,
        email = email,
        password = BCryptPasswordEncoder().encode(password.lowercase()),
        tel = tel,
        nickname = nickname,
        provider = provider.name
    )
}
