package com.unimal.common.dto.kafka.user

import com.unimal.common.enums.UserStatus
import java.time.LocalDateTime

data class UpdateUser(
    val email: String,
    val nickname: String? = null,
    val profileImage: String? = null,
    val withdrawalAt: LocalDateTime? = null,
    val status: UserStatus? = null,
    val fcmToken: String? = null,
)
