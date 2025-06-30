package com.unimal.notification.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class MailServiceTest {

    @Autowired
    lateinit var notificationService: NotificationService

    @Test
    fun `test`() {
        notificationService.mailAuthenticationCodeSend("123435", "eodud4976@gmail.com")
    }

}