package com.unimal.notification.service.navercloud

import org.junit.jupiter.api.Test

import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * 순수 단위 테스트 — 스프링 컨텍스트를 띄우지 않는다.
 *
 * makeSignature 는 인자만 받아 서명 문자열을 만드는 함수라 컨텍스트가 필요
 * 없는데, `@SpringBootTest` 탓에 notification 모듈 컨텍스트 로딩 실패에
 * 휩쓸려 같이 죽고 있었다.
 */
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