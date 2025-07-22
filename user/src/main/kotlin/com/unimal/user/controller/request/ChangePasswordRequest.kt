package com.unimal.user.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

interface ChangePasswordInterface {
    var email: String?
    val oldPassword: String
    val newPassword: String
}

data class ChangePasswordRequest(
    override var email: String? = null,

    @field:NotBlank
    override val oldPassword: String,
    @field:NotBlank
    override val newPassword: String
): ChangePasswordInterface


data class VerifyChangePasswordRequest(
    @field:[NotBlank NotNull]
    override var email: String?,
    @field:NotBlank
    override val oldPassword: String,
    @field:NotBlank
    override val newPassword: String
): ChangePasswordInterface