package com.unimal.user.service.login.apple

import com.google.gson.Gson
import com.unimal.user.service.login.apple.dto.AppleIdTokenPayload
import com.unimal.user.service.login.apple.dto.AppleJwk
import com.unimal.user.service.login.apple.dto.AppleJwkSet
import com.unimal.user.service.login.apple.dto.AppleJwtHeader
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Apple identityToken(JWT) 검증기.
 *
 * 클라이언트가 보낸 토큰을 그대로 신뢰하면 누구나 임의의 이메일로 로그인할 수 있으므로
 * 반드시 애플 공개키로 서명을 검증하고 iss / aud / exp를 확인한 뒤 sub와 email을 꺼내 쓴다.
 *
 * 공개키는 애플이 주기적으로 로테이션하므로 kid 기준으로 캐시하고, 캐시에 없으면 다시 받아온다.
 * (별도 JWK 라이브러리 없이 JDK KeyFactory로 modulus/exponent를 RSA 공개키로 복원한다)
 */
@Component
class AppleIdentityTokenVerifier(
    private val appleProperties: AppleProperties,
) {
    private val logger = KotlinLogging.logger {}
    private val gson = Gson()
    // 애플 응답이 지연되면 로그인 스레드가 물리므로 타임아웃을 반드시 건다.
    private val restTemplate: RestTemplate = RestTemplateBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .readTimeout(Duration.ofSeconds(5))
        .build()
    private val publicKeyCache = ConcurrentHashMap<String, RSAPublicKey>()
    private val lastRefreshedAt = AtomicLong(0)

    fun verify(identityToken: String): AppleIdTokenPayload {
        val kid = extractKid(identityToken)
        val publicKey = findPublicKey(kid)

        val claims: Claims = try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(identityToken)
                .payload
        } catch (e: Exception) {
            logger.warn { "애플 identityToken 검증 실패: ${e.message}" }
            throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)
        }

        if (claims.issuer != APPLE_ISSUER) {
            logger.warn { "애플 identityToken iss 불일치: ${claims.issuer}" }
            throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)
        }

        // aud claim이 아예 없는 토큰도 있으므로 null-safe 하게 비교한다.
        if (claims.audience?.contains(appleProperties.clientId) != true) {
            logger.warn { "애플 identityToken aud 불일치: expected=${appleProperties.clientId}, actual=${claims.audience}" }
            throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)
        }

        val sub = claims.subject ?: throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)

        return AppleIdTokenPayload(
            sub = sub,
            email = claims["email"] as? String,
            emailVerified = booleanFlag(claims["email_verified"]),
            isPrivateEmail = booleanFlag(claims["is_private_email"]),
        )
    }

    /**
     * 애플은 email_verified / is_private_email을 Boolean 또는 "true" 문자열로 내려준다.
     */
    private fun booleanFlag(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is String -> value.equals("true", ignoreCase = true)
        else -> false
    }

    private fun extractKid(identityToken: String): String {
        val encodedHeader = identityToken.substringBefore(".", "")
        if (encodedHeader.isBlank()) throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)

        val kid = try {
            val decoded = String(Base64.getUrlDecoder().decode(encodedHeader), Charsets.UTF_8)
            gson.fromJson(decoded, AppleJwtHeader::class.java)?.kid
        } catch (e: Exception) {
            logger.warn { "애플 identityToken 헤더 파싱 실패: ${e.message}" }
            null
        }

        return kid ?: throw LoginException(ErrorCode.APPLE_TOKEN_INVALID.message)
    }

    private fun findPublicKey(kid: String): RSAPublicKey {
        publicKeyCache[kid]?.let { return it }

        // 모르는 kid로 계속 요청이 들어오면 애플을 그만큼 두드리게 되므로 재조회 간격을 둔다.
        // 단, 캐시가 비어 있는 상태(콜드 스타트 / 직전 조회 실패)에서는 스로틀을 걸지 않는다.
        // 걸어버리면 최초 조회 한 번 실패했을 때 5분 동안 애플 로그인이 전부 막힌다.
        val now = System.currentTimeMillis()
        val previous = lastRefreshedAt.get()
        if (publicKeyCache.isNotEmpty() && now - previous < KEY_REFRESH_INTERVAL_MILLIS) {
            throw LoginException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND.message)
        }
        // 동시 요청이 몰려도 실제 조회는 한 번만 나가도록 CAS로 선점한다.
        if (!lastRefreshedAt.compareAndSet(previous, now)) {
            throw LoginException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND.message)
        }

        refreshPublicKeys()
        return publicKeyCache[kid] ?: throw LoginException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND.message)
    }

    private fun refreshPublicKeys() {
        val jwkSet = try {
            val body = restTemplate.getForObject(APPLE_JWKS_URL, String::class.java)
            gson.fromJson(body, AppleJwkSet::class.java)
        } catch (e: Exception) {
            logger.error(e) { "애플 공개키 조회 실패" }
            throw LoginException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND.message)
        }

        jwkSet?.keys.orEmpty().forEach { jwk ->
            runCatching { publicKeyCache[jwk.kid] = jwk.toRsaPublicKey() }
                .onFailure { logger.warn(it) { "애플 공개키 변환 실패: kid=${jwk.kid}" } }
        }
    }

    private fun AppleJwk.toRsaPublicKey(): RSAPublicKey {
        val modulus = BigInteger(1, Base64.getUrlDecoder().decode(n))
        val exponent = BigInteger(1, Base64.getUrlDecoder().decode(e))
        return KeyFactory.getInstance("RSA")
            .generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey
    }

    companion object {
        const val APPLE_ISSUER = "https://appleid.apple.com"
        const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
        private const val KEY_REFRESH_INTERVAL_MILLIS = 5 * 60 * 1000L
    }
}
