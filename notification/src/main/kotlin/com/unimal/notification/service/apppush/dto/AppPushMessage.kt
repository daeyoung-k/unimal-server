package com.unimal.notification.service.apppush.dto

data class AppPushSend(
    val token: String,
    val title: String? = "",
    val body: String,
    val data: Map<String, String> = emptyMap()
)

data class AppPushMulticastSend(
    val tokens: List<String>,
    val title: String? = "",
    val body: String,
    val data: Map<String, String> = emptyMap()
)