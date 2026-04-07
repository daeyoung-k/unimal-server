package com.unimal.board.kafka.topics.dto

import com.unimal.board.service.post.enums.UserCountCalculateType

data class UserCountIssue(
    val email: String,
    val type: UserCountCalculateType
)
