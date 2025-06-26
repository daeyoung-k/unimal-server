package com.unimal.notification.service.navercloud

import org.junit.jupiter.api.Test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertContains
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("local")
class NaverCloudSignatureUtilTest {

    companion object {
        private val naverCloudAccessKey = "test-access-key"
        private val naverCloudSecretKey = "test-secret-key"
    }

    @Test
    fun `builderMessage - message를 확인한다`() {
        // Given
        val naverCloudSignatureUtil = NaverCloudSignatureUtil(
            naverCloudAccessKey = naverCloudAccessKey,
            naverCloudSecretKey = naverCloudSecretKey,
        )
        // When
        val message = naverCloudSignatureUtil.builderMessage()

        // Then
        assertNotNull(message)
        assertContains(message, naverCloudAccessKey)
    }

    @Test
    fun `makeSignature - 만들어진 서명을 확인한다`() {
        // Given
        val naverCloudSignatureUtil = NaverCloudSignatureUtil(
            naverCloudAccessKey = naverCloudAccessKey,
            naverCloudSecretKey = naverCloudSecretKey,
        )

        // When
        val signature = naverCloudSignatureUtil.makeSignature()

        println(signature)
        println(System.currentTimeMillis().toString())

        // Then
        assertNotNull(signature)
        assertNotEquals("", signature)
    }


}