package com.unimal.user.service.login

import com.unimal.user.service.member.MemberObject
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LoginObjectsTest {

    @Autowired
    lateinit var memberObject: MemberObject

    @Test
    fun `멤버가 있는지 확인한다`() {}
}