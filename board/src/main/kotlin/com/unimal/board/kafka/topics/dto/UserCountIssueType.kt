package com.unimal.board.kafka.topics.dto

import com.unimal.board.service.post.enums.UserCountCalculateType

data class UserCountIssueType(
    val email: String,
    val type: UserCountCalculateType
)
