package com.unimal.user.controller.request

import jakarta.validation.constraints.NotBlank


data class EmailRequest(
    @field:NotBlank
    val email: String,
)

data class TelAuthCodeRequest(
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val tel: String,
)

data class EmailAuthCodeVerifyRequest(
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val email: String
)

data class TelAuthCodeVerifyRequest(
    @field:NotBlank
    val code: String,
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val tel: String
)