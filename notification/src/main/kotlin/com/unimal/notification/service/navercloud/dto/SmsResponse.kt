package com.unimal.notification.service.navercloud.dto

data class SmsResponse(
    val requestId: String,
    val requestTime: String,
    val statusCode: String,
    val statusName: String,
)
