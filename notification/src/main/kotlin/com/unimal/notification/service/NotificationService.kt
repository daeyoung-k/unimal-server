package com.unimal.notification.service

import jakarta.mail.internet.InternetAddress
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class NotificationService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
) {

    fun authenticationCodeSendMail(
        email: String,
        code: String,
    ) {
        val message = javaMailSender.createMimeMessage()
        val context = Context().apply {
            setVariable("code", code)
        }
        val htmlContent = templateEngine.process("authentication-mail", context)
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setFrom(InternetAddress("support@unimal.co.kr", "Unimal Support", "UTF-8"))
        helper.setSubject("[Unimal] 이메일 인증 코드")
        helper.setTo(email)
        helper.setText(htmlContent, true)

        javaMailSender.send(message)
    }

}