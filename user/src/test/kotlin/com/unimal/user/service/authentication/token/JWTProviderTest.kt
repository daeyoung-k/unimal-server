package com.unimal.user.service.authentication.token

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class JWTProviderTest {

    @Autowired
    private val provider = JWTProvider()

    @Test
    fun `secretKey 암호화 확인`() {
        provider.createAccessToken()
    }


}