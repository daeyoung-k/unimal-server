package com.unimal.user.controller.request

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val oldPassword: String,
    @field:NotBlank
    val newPassword: String
)
