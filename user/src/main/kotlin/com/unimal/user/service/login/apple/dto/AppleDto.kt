package com.unimal.user.service.login.apple.dto

import com.google.gson.annotations.SerializedName

/**
 * Apple JWKS(https://appleid.apple.com/auth/keys) 응답.
 * Gson은 Kotlin 기본값을 무시하고 Unsafe로 객체를 만들기 때문에 nullable로 둔다.
 */
data class AppleJwkSet(
    val keys: List<AppleJwk>? = null
)

data class AppleJwk(
    val kty: String,
    val kid: String,
    val use: String? = null,
    val alg: String? = null,
    val n: String,
    val e: String,
)

/** identityToken 헤더 (kid 추출용) */
data class AppleJwtHeader(
    val kid: String? = null,
    val alg: String? = null,
)

/** identityToken 검증 결과 */
data class AppleIdTokenPayload(
    /** Apple이 발급한 사용자 고유 식별자. 이메일과 달리 절대 바뀌지 않는다. */
    val sub: String,
    val email: String?,
    val emailVerified: Boolean,
    val isPrivateEmail: Boolean,
)

/** POST https://appleid.apple.com/auth/token 응답 */
data class AppleTokenResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    @SerializedName("id_token") val idToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    val error: String? = null,
)
