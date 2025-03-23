package com.unimal.user.service.login

interface Login {
    fun provider(): LoginType
    fun getInfo(token: String)
}