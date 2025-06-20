package com.unimal.common.dto.kafka

import java.time.LocalDateTime

data class SignInUser(
    val email: String,
    val name: String?,
    val nickname: String?,
    val profileImageUrl: String? = null,
    val withdrawalAt: LocalDateTime? = null
)