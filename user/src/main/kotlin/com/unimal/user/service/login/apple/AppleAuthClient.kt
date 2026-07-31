package com.unimal.user.service.login.apple

import com.google.gson.Gson
import com.unimal.user.service.login.apple.dto.AppleTokenResponse
import io.jsonwebtoken.Jwts
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.util.Base64
import java.util.Date

/**
 * Apple 서버 API 클라이언트.
 *
 * 1) authorization_code -> refresh_token 교환
 * 2) 탈퇴 시 refresh_token revoke
 *
 * 애플은 "계정 삭제 기능을 제공하면서 Sign in with Apple을 쓰는 앱"에 대해
 * 삭제 시점의 토큰 revoke를 요구한다(TN3194 / 심사 가이드 5.1.1(v)).
 *
 * 인증에 쓰이는 client_secret은 고정 문자열이 아니라
 * .p8 개인키로 ES256 서명한 단기 JWT다. (최대 6개월, 여기서는 1시간)
 */
@Component
class AppleAuthClient(
    private val appleProperties: AppleProperties,
) {
    private val logger = KotlinLogging.logger {}
    private val gson = Gson()
    private val restTemplate: RestTemplate = RestTemplateBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .readTimeout(Duration.ofSeconds(5))
        .build()

    /**
     * 로그인 시 받은 authorizationCode를 refresh_token으로 교환한다.
     * authorization_code는 5분 내 1회만 사용 가능하므로 실패해도 로그인 자체는 막지 않는다.
     */
    fun exchangeAuthorizationCode(authorizationCode: String): AppleTokenResponse? {
        if (!appleProperties.serverApiEnabled) {
            logger.warn { "애플 서버 API 설정(team-id/key-id/private-key)이 없어 토큰 교환을 건너뜁니다." }
            return null
        }

        val params = LinkedMultiValueMap<String, String>().apply {
            add("client_id", appleProperties.clientId)
            add("client_secret", createClientSecret())
            add("code", authorizationCode)
            add("grant_type", "authorization_code")
        }

        val response = restTemplate.postForObject(
            APPLE_TOKEN_URL,
            HttpEntity(params, formHeaders()),
            String::class.java
        )
        return gson.fromJson(response, AppleTokenResponse::class.java)
    }

    /**
     * refresh_token을 폐기한다. 폐기되면 해당 사용자의 Sign in with Apple 연결이 끊어진다.
     */
    fun revoke(refreshToken: String) {
        if (!appleProperties.serverApiEnabled) {
            logger.warn { "애플 서버 API 설정이 없어 revoke를 건너뜁니다." }
            return
        }

        val params = LinkedMultiValueMap<String, String>().apply {
            add("client_id", appleProperties.clientId)
            add("client_secret", createClientSecret())
            add("token", refreshToken)
            add("token_type_hint", "refresh_token")
        }

        restTemplate.postForObject(
            APPLE_REVOKE_URL,
            HttpEntity(params, formHeaders()),
            String::class.java
        )
        logger.info { "애플 refresh_token revoke 완료" }
    }

    private fun formHeaders() = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_FORM_URLENCODED
    }

    /**
     * Apple 서버 API 인증용 client_secret(JWT, ES256) 생성.
     */
    fun createClientSecret(): String {
        val now = Date()
        return Jwts.builder()
            .header().keyId(appleProperties.keyId).and()
            .issuer(appleProperties.teamId)
            .issuedAt(now)
            .expiration(Date(now.time + CLIENT_SECRET_EXPIRE_MILLIS))
            .audience().add(APPLE_AUDIENCE).and()
            .subject(appleProperties.clientId)
            .signWith(loadPrivateKey(), Jwts.SIG.ES256)
            .compact()
    }

    private fun loadPrivateKey(): PrivateKey {
        val keyBytes = Base64.getDecoder().decode(appleProperties.privateKeyBody())
        return KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
    }

    companion object {
        const val APPLE_AUDIENCE = "https://appleid.apple.com"
        const val APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token"
        const val APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke"
        const val CLIENT_SECRET_EXPIRE_MILLIS = 60 * 60 * 1000L
    }
}
