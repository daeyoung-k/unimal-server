package com.unimal.notification.service.navercloud

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class NaverCloudSignatureUtil(
    @Value("\${custom.navercloud.access-key}")
    private val naverCloudAccessKey: String,
    @Value("\${custom.navercloud.secret-key}")
    private val naverCloudSecretKey: String

) {

    fun makeSignature(): String {
        val message = builderMessage()

        val mac = Mac.getInstance("HmacSHA256")
        val sks = SecretKeySpec(naverCloudSecretKey.toByteArray(charset("UTF-8")), "HmacSHA256")
        mac.init(sks)
        val rawHmac = mac.doFinal(message.toByteArray())
        return Base64.getEncoder().encodeToString(rawHmac)

    }

    fun builderMessage(): String {
        val space = " "
        val newLine = "\n"
        val method = "GET"
        val url = "/photos/puppy.jpg?query1=&query2"
        val timestamp = System.currentTimeMillis().toString()

        return StringBuilder()
            .append(method)
            .append(space)
            .append(url)
            .append(newLine)
            .append(timestamp)
            .append(newLine)
            .append(naverCloudAccessKey)
            .toString()
    }
}

