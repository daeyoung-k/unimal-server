package com.unimal.common.dto.kafka

import java.time.LocalDateTime

data class SignInUser(
    val email: String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImage: String? = null,
    val withdrawalAt: LocalDateTime? = null
)