package com.unimal.user.controller.request

import jakarta.validation.constraints.NotBlank


data class EmailCodeRequest(
    @field:NotBlank
    val email: String,
)

data class TelCodeRequest(
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val tel: String,
)

data class EmailCodeVerifyRequest(
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val email: String
)

data class TelVerifyCodeRequest(
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val tel: String
)