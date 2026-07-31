package com.unimal.user.service.login.apple

import com.unimal.webcommon.exception.LoginException
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class AppleIdentityTokenVerifierTest {

    private val clientId = "com.unimal.ios.stomap"
    private lateinit var keyPair: KeyPair
    private lateinit var verifier: AppleIdentityTokenVerifier

    @BeforeEach
    fun setUp() {
        keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        verifier = AppleIdentityTokenVerifier(
            AppleProperties(clientId = clientId, teamId = "TEAM", keyId = "KEY", rawPrivateKey = "")
        )
        seedPublicKey(TEST_KID, keyPair.public as RSAPublicKey)
    }

    @Test
    fun `정상 토큰이면 sub와 email을 꺼낸다`() {
        val token = createToken(email = "tester@privaterelay.appleid.com", isPrivateEmail = true)

        val payload = verifier.verify(token)

        assertEquals("001234.abcdef.0000", payload.sub)
        assertEquals("tester@privaterelay.appleid.com", payload.email)
        assertTrue(payload.isPrivateEmail)
        assertTrue(payload.emailVerified)
    }

    @Test
    fun `email claim이 없어도 sub는 꺼낸다`() {
        val payload = verifier.verify(createToken(email = null))

        assertEquals("001234.abcdef.0000", payload.sub)
        assertEquals(null, payload.email)
        assertFalse(payload.isPrivateEmail)
    }

    @Test
    fun `aud가 우리 번들ID가 아니면 거부한다`() {
        val token = createToken(audience = "com.someone.else.app")

        assertThrows<LoginException> { verifier.verify(token) }
    }

    @Test
    fun `iss가 애플이 아니면 거부한다`() {
        val token = createToken(issuer = "https://evil.example.com")

        assertThrows<LoginException> { verifier.verify(token) }
    }

    @Test
    fun `만료된 토큰은 거부한다`() {
        val token = createToken(expiration = Date(System.currentTimeMillis() - 60_000))

        assertThrows<LoginException> { verifier.verify(token) }
    }

    @Test
    fun `다른 키로 서명된 토큰은 거부한다`() {
        val attacker = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val token = createToken(signingKeyPair = attacker)

        assertThrows<LoginException> { verifier.verify(token) }
    }

    private fun createToken(
        issuer: String = AppleIdentityTokenVerifier.APPLE_ISSUER,
        audience: String = clientId,
        subject: String = "001234.abcdef.0000",
        email: String? = "tester@privaterelay.appleid.com",
        isPrivateEmail: Boolean = false,
        expiration: Date = Date(System.currentTimeMillis() + 600_000),
        signingKeyPair: KeyPair = keyPair,
    ): String {
        val builder = Jwts.builder()
            .header().keyId(TEST_KID).and()
            .issuer(issuer)
            .audience().add(audience).and()
            .subject(subject)
            .issuedAt(Date())
            .expiration(expiration)
            .claim("email_verified", "true")
            .claim("is_private_email", isPrivateEmail)

        email?.let { builder.claim("email", it) }

        return builder.signWith(signingKeyPair.private, Jwts.SIG.RS256).compact()
    }

    @Suppress("UNCHECKED_CAST")
    private fun seedPublicKey(kid: String, publicKey: RSAPublicKey) {
        val field = AppleIdentityTokenVerifier::class.java.getDeclaredField("publicKeyCache")
        field.isAccessible = true
        (field.get(verifier) as ConcurrentHashMap<String, RSAPublicKey>)[kid] = publicKey
    }

    companion object {
        private const val TEST_KID = "test-kid"
    }
}
