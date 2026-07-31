package com.unimal.user.service.login.apple

import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Base64

class AppleAuthClientTest {

    @Test
    fun `client_secret은 ES256 JWT로 만들어지고 애플 규격을 만족한다`() {
        val keyPair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair()
        val p8Body = Base64.getEncoder().encodeToString(keyPair.private.encoded)

        val properties = AppleProperties(
            clientId = "com.unimal.ios.stomap",
            teamId = "ABCDE12345",
            keyId = "KEYID67890",
            rawPrivateKey = "-----BEGIN PRIVATE KEY-----\\n$p8Body\\n-----END PRIVATE KEY-----",
        )
        val clientSecret = AppleAuthClient(properties).createClientSecret()

        val parsed = Jwts.parser()
            .verifyWith(keyPair.public)
            .build()
            .parseSignedClaims(clientSecret)

        assertEquals("KEYID67890", parsed.header.keyId)
        assertEquals("ABCDE12345", parsed.payload.issuer)
        assertEquals("com.unimal.ios.stomap", parsed.payload.subject)
        assertTrue(parsed.payload.audience.contains(AppleAuthClient.APPLE_AUDIENCE))
    }

    @Test
    fun `서버 API 설정이 비어 있으면 비활성 상태로 판단한다`() {
        val properties = AppleProperties(
            clientId = "com.unimal.ios.stomap",
            teamId = "",
            keyId = "",
            rawPrivateKey = "",
        )

        assertFalse(properties.serverApiEnabled)
    }
}
