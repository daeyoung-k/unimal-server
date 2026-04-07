package com.unimal.common.dto.kafka.post

import com.unimal.common.enums.AppPushType

data class PostAppPushEvent(
    val token: String,
    val type: AppPushType,
    val targetId: String,
    val title: String,
    val body: String
)
