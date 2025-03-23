package com.unimal.common.dto

data class CommonResponse(
    val code: Int? = 200,
    val message: String? = "Success",
    val data: Any? = emptyMap<String, String>()
)