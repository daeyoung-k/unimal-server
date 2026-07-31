package com.unimal.user.service.login.apple

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Sign in with Apple 설정값.
 *
 * - clientId  : iOS 앱의 Bundle ID (identityToken의 aud claim과 일치해야 한다)
 * - teamId    : Apple Developer 팀 ID (client_secret JWT의 iss)
 * - keyId     : Sign in with Apple 용 .p8 키의 Key ID (client_secret JWT 헤더의 kid)
 * - privateKey: .p8 파일 내용. 환경변수로 주입할 때는 개행을 "\n" 리터럴로 넣어도 되도록 처리한다.
 */
@Component
class AppleProperties(
    @Value("\${custom.apple.client-id}") val clientId: String,
    @Value("\${custom.apple.team-id:}") val teamId: String,
    @Value("\${custom.apple.key-id:}") val keyId: String,
    @Value("\${custom.apple.private-key:}") private val rawPrivateKey: String,
) {
    /** revoke / token 교환 기능을 쓸 수 있는 설정이 모두 갖춰졌는지 여부 */
    val serverApiEnabled: Boolean
        get() = teamId.isNotBlank() && keyId.isNotBlank() && rawPrivateKey.isNotBlank()

    /** PEM 헤더/푸터와 공백을 제거한 순수 base64 본문 */
    fun privateKeyBody(): String = rawPrivateKey
        .replace("\\n", "\n")
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace(Regex("\\s"), "")
}
