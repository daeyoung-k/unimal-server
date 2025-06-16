package com.unimal.user.controller.request

data class InfoUpdateRequest(
    val name: String?,
    val nickname: String?,
    val tel: String?,
    val introduction: String?,
    val birthday: String?,
    val gender: String?
)
