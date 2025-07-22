package com.unimal.notification.service.navercloud


import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object NaverCloudSignatureUtil{
    fun makeSignature(
        timestamp: String,
        naverCloudAccessKey: String,
        naverCloudSecretKey: String,
        naverCloudSmsServiceId: String,
    ): String {
        val method = "POST"
        val url = "/sms/v2/services/$naverCloudSmsServiceId/messages"

        val message = "$method $url\n$timestamp\n$naverCloudAccessKey"

        val signingKey = SecretKeySpec(naverCloudSecretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }

}

