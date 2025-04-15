package com.unimal.user.service.authentication.login

fun interface Login {
    fun provider(): LoginType
}