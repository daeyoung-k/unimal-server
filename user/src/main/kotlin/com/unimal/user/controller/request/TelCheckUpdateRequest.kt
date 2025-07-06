package com.unimal.user.controller.request

import jakarta.validation.constraints.NotBlank

data class TelCheckUpdateRequest(
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val tel: String,
)
