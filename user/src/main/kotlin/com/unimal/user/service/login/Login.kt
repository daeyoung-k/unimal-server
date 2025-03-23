package com.unimal.user.service.login

fun interface Login {
    fun provider(): LoginType
}