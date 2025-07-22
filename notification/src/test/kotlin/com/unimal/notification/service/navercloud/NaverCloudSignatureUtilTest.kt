package com.unimal.notification.service.navercloud

import org.junit.jupiter.api.Test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("local")
class NaverCloudSignatureUtilTest {

    @Test
    fun `makeSignature - 만들어진 서명을 확인한다`() {
        // Given
        val timestamp = System.currentTimeMillis().toString()
        val naverCloudAccessKey = "test-access-key"
        val naverCloudSecretKey = "test-secret-key"
        val naverCloudSmsServiceId = "test-sms-service-id"

        // When
        val signature = NaverCloudSignatureUtil.makeSignature(
            timestamp,
            naverCloudAccessKey,
            naverCloudSecretKey,
            naverCloudSmsServiceId
        )

        println(signature)

        // Then
        assertNotNull(signature)
        assertNotEquals("", signature)
    }


}