package com.unimal.notification.service.navercloud


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.notification.service.navercloud.dto.SmsBody
import com.unimal.notification.service.navercloud.dto.SmsResponse
import com.unimal.webcommon.exception.ApiCallException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NaverCloudSmsManager(
    @Value("\${custom.naver-cloud.access-key}")
    private val naverCloudAccessKey: String,
    @Value("\${custom.naver-cloud.secret-key}")
    private val naverCloudSecretKey: String,
    @Value("\${custom.naver-cloud.sms-service-id}")
    private val naverCloudSmsServiceId: String,
) {
    private val logger = KotlinLogging.logger("NaverCloudSmsManager")

    fun sendSms(smsBody: SmsBody) {
        val restTemplate = RestTemplate()
        val url = "https://sens.apigw.ntruss.com/sms/v2/services/$naverCloudSmsServiceId/messages"

        val timestamp = System.currentTimeMillis().toString()
        val signatureKey = NaverCloudSignatureUtil.makeSignature(
            timestamp,
            naverCloudAccessKey,
            naverCloudSecretKey,
            naverCloudSmsServiceId
        )

        val headers = HttpHeaders()
        headers.add("Content-Type", APPLICATION_JSON_VALUE)
        headers.add("x-ncp-apigw-timestamp", timestamp)
        headers.add("x-ncp-iam-access-key", naverCloudAccessKey)
        headers.add("x-ncp-apigw-signature-v2", signatureKey)

        val entity = HttpEntity(smsBody, headers)
        try {
            val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)

            val mapper = jacksonObjectMapper()
            val root = mapper.readTree(response.body)
            val smsResponse = mapper.treeToValue(root, SmsResponse::class.java)
            logger.info { "SMS response: $smsResponse" }

        } catch (e: Exception) {
            e.printStackTrace()
            throw ApiCallException("SMS 전송 실패: ${e.message}")
        }


    }
}