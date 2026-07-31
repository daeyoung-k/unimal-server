package com.unimal.user.service.login

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
import com.unimal.common.enums.UserStatus
import com.unimal.user.controller.request.AppleLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.login.apple.AppleAuthClient
import com.unimal.user.service.login.apple.dto.AppleTokenResponse
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.token.TokenManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginServiceAppleTest {

    private val kakaoLoginObject: LoginInterface = mockk()
    private val naverLoginObject: LoginInterface = mockk()
    private val googleLoginObject: LoginInterface = mockk()
    private val appleLoginObject: LoginInterface = mockk()
    private val manualLoginObject: LoginInterface = mockk()
    private val appleAuthClient: AppleAuthClient = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val memberObject: MemberObject = mockk(relaxed = true)
    private val memberKafkaTopic: MemberKafkaTopic = mockk(relaxed = true)
    private val memberRepository: MemberRepository = mockk(relaxed = true)

    private val loginService = LoginService(
        kakaoLoginObject = kakaoLoginObject,
        naverLoginObject = naverLoginObject,
        googleLoginObject = googleLoginObject,
        appleLoginObject = appleLoginObject,
        manualLoginObject = manualLoginObject,
        appleAuthClient = appleAuthClient,
        tokenManager = tokenManager,
        memberObject = memberObject,
        memberKafkaTopic = memberKafkaTopic,
        memberRepository = memberRepository,
    )

    @BeforeEach
    fun setUpRepository() {
        // JpaRepository.save 는 <S : T> S save(S) 라 타입 소거 후 반환형이 Object 가 된다.
        // relaxed 목이 만들어 주는 기본 Object 를 Member 로 캐스팅하다 ClassCastException 이 나므로
        // 넘긴 엔티티를 그대로 돌려주도록 명시적으로 스텁한다.
        every { memberRepository.save(any<Member>()) } answers { firstArg<Member>() }
    }

    private val email = "j86h2pvz6s@privaterelay.appleid.com"
    private val sub = "001234.abcdef.0000"

    /**
     * 회귀 방지 — 이미 refresh_token을 들고 있어도 로그인마다 갱신해야 한다.
     * 사용자가 iOS 설정에서 앱 연결을 끊었다 다시 로그인하면 기존 토큰은 죽고
     * 새 연결이 생기는데, 저장값을 그대로 믿으면 탈퇴 시 revoke가 조용히 실패한다.
     */
    @Test
    fun `이미 refresh_token이 있어도 로그인할 때마다 갱신한다`() {
        val member = appleMember(appleRefreshToken = "OLD_DEAD_TOKEN")
        stubLogin(member)
        every { appleAuthClient.exchangeAuthorizationCode("code") } returns
            AppleTokenResponse(refreshToken = "NEW_TOKEN")

        loginService.login(appleRequest(authorizationCode = "code"))

        verify(exactly = 1) { appleAuthClient.exchangeAuthorizationCode("code") }
        assertEquals("NEW_TOKEN", member.appleRefreshToken)
    }

    @Test
    fun `authorizationCode가 없으면 애플을 호출하지 않는다`() {
        val member = appleMember(appleRefreshToken = "EXISTING")
        stubLogin(member)

        loginService.login(appleRequest(authorizationCode = null))

        verify(exactly = 0) { appleAuthClient.exchangeAuthorizationCode(any()) }
        assertEquals("EXISTING", member.appleRefreshToken)
    }

    @Test
    fun `토큰 교환이 실패해도 로그인은 진행되고 기존 토큰은 유지된다`() {
        val member = appleMember(appleRefreshToken = "EXISTING")
        stubLogin(member)
        every { appleAuthClient.exchangeAuthorizationCode("code") } throws RuntimeException("apple down")

        loginService.login(appleRequest(authorizationCode = "code"))

        assertEquals("EXISTING", member.appleRefreshToken)
        verify(exactly = 1) {
            tokenManager.createJwtToken(email, "홍길동", LoginType.APPLE, emptyList())
        }
    }

    @Test
    fun `애플 회원 탈퇴 시 refresh_token을 revoke 한다`() {
        val member = appleMember(appleRefreshToken = "LIVE_TOKEN")
        every { memberObject.getEmailProviderMember(email, LoginType.APPLE) } returns member

        loginService.withdrawal(commonUserInfo(LoginType.APPLE))

        verify(exactly = 1) { appleAuthClient.revoke("LIVE_TOKEN") }
        assertEquals(UserStatus.WITHDRAWAL, member.status)
    }

    @Test
    fun `애플이 아닌 회원 탈퇴에서는 revoke를 호출하지 않는다`() {
        val member = Member(
            email = "kakao@unimal.co.kr",
            provider = LoginType.KAKAO.name,
            nickname = "카카오유저",
            tel = "01012345678",
        )
        every {
            memberObject.getEmailProviderMember("kakao@unimal.co.kr", LoginType.KAKAO)
        } returns member

        loginService.withdrawal(
            commonUserInfo(LoginType.KAKAO).copy(email = "kakao@unimal.co.kr")
        )

        verify(exactly = 0) { appleAuthClient.revoke(any()) }
    }

    private fun stubLogin(member: Member) {
        val userInfo = userInfo()
        every { appleLoginObject.getUserInfo(any<AppleLoginRequest>()) } returns userInfo
        every { appleLoginObject.getMember(userInfo) } returns member
    }

    private fun appleRequest(authorizationCode: String?) = AppleLoginRequest(
        identityToken = "identity-token",
        authorizationCode = authorizationCode,
        name = "홍길동",
        nickname = "홍길동",
    )

    private fun userInfo() = UserInfo(
        provider = LoginType.APPLE.name,
        email = email,
        providerId = sub,
        name = "홍길동",
        nickname = "홍길동",
    )

    private fun appleMember(appleRefreshToken: String?) = Member(
        email = email,
        provider = LoginType.APPLE.name,
        providerId = sub,
        appleRefreshToken = appleRefreshToken,
        nickname = "홍길동",
        tel = "01012345678",
        status = UserStatus.ACTIVE,
    )

    private fun commonUserInfo(provider: LoginType) = CommonUserInfo(
        email = email,
        nickname = "홍길동",
        roles = listOf("USER"),
        provider = provider.name,
        tokenType = TokenType.REFRESH,
    )
}
