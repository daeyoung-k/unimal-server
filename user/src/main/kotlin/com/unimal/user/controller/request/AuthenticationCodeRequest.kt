package com.unimal.user.controller.request


data class EmailCodeRequest(
    val email: String,
)

data class TelCodeRequest(
    val email: String,
    val tel: String,
)

data class EmailCodeVerifyRequest(
    val code: String,
    val email: String
)

data class TelVerifyCodeRequest(
    val code: String,
    val email: String,
    val tel: String
)