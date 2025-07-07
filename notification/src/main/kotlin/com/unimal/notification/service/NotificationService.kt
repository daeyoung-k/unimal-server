package com.unimal.notification.service

import com.unimal.notification.service.authcode.CreateAuthCodeObject
import com.unimal.notification.service.navercloud.NaverCloudSmsManager
import com.unimal.notification.service.navercloud.dto.SmsBody
import com.unimal.notification.service.navercloud.enums.SmsTemplate
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class NotificationService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val naverCloudSmsManager: NaverCloudSmsManager,
    private val createAuthCodeObject: CreateAuthCodeObject,

    @Value("\${custom.auth-tel-number}")
    private val authTelNumber: String
) {

    fun mailAuthenticationCodeSend(
        email: String,
    ) {
        val message = javaMailSender.createMimeMessage()
        val authCode = createAuthCodeObject.createMailAuthCode(email)
        val context = Context().apply {
            setVariable("code", authCode)
        }
        val htmlContent = templateEngine.process("authentication-mail", context)
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setFrom(InternetAddress("support@unimal.co.kr", "Unimal Support", "UTF-8"))
        helper.setSubject("[Unimal] 이메일 인증 코드")
        helper.setTo(email)
        helper.setText(htmlContent, true)

        javaMailSender.send(message)
    }

    fun telAuthenticationCodeSend(
        email: String,
        tel: String,
    ) {
        val authCode = createAuthCodeObject.createMailTelAuthCode(email, tel)
        naverCloudSmsManager.sendSms(
            SmsBody(
                from = authTelNumber,
                content = SmsTemplate.AUTH_CODE.template(authCode),
                toList = listOf(tel)
            )
        )
    }

    fun telFindAuthCodeSend(
        tel: String
    ) {
        val authCode = createAuthCodeObject.createTelAuthCode(tel)
        naverCloudSmsManager.sendSms(
            SmsBody(
                from = authTelNumber,
                content = SmsTemplate.AUTH_CODE.template(authCode),
                toList = listOf(tel)
            )
        )

    }



}