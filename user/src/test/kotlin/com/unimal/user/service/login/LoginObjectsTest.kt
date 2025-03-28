package com.unimal.user.service.login

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LoginObjectsTest {

    @Autowired
    lateinit var loginObjects: LoginObjects

    @Test
    fun `멤버가 있는지 확인한다`() {}
}