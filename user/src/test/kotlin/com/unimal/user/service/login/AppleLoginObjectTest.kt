package com.unimal.user.service.login

import com.unimal.user.controller.request.AppleLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.service.login.apple.AppleIdentityTokenVerifier
import com.unimal.user.service.login.apple.dto.AppleIdTokenPayload
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.LoginException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AppleLoginObjectTest {

    private val memberObject = mockk<MemberObject>()
    private val memberRepository = mockk<MemberRepository>(relaxed = true)
    private val verifier = mockk<AppleIdentityTokenVerifier>()
    private val appleLoginObject = AppleLoginObject(memberObject, memberRepository, verifier)

    @BeforeEach
    fun setUpRepository() {
        // JpaRepository.save 는 <S : T> S save(S) 라 타입 소거 후 반환형이 Object 가 된다.
        // relaxed 목이 만들어 주는 기본 Object 를 Member 로 캐스팅하다 ClassCastException 이 나므로
        // 넘긴 엔티티를 그대로 돌려주도록 명시적으로 스텁한다.
        every { memberRepository.save(any<Member>()) } answers { firstArg<Member>() }
    }

    private val sub = "001234.abcdef.0000"

    @Test
    fun `identityToken에서 이메일과 sub를 꺼내 UserInfo를 만든다`() {
        every { verifier.verify("token") } returns payload(email = "a@privaterelay.appleid.com")

        val userInfo = appleLoginObject.getUserInfo(
            AppleLoginRequest(identityToken = "token", name = "대영")
        )

        assertEquals(LoginType.APPLE.name, userInfo.provider)
        assertEquals("a@privaterelay.appleid.com", userInfo.email)
        assertEquals(sub, userInfo.providerId)
        assertEquals("대영", userInfo.nickname)
    }

    @Test
    fun `email claim이 없으면 기존 회원의 이메일을 사용한다`() {
        every { verifier.verify("token") } returns payload(email = null)
        every { memberRepository.findByProviderAndProviderId(LoginType.APPLE.name, sub) } returns
            member(email = "old@privaterelay.appleid.com")

        val userInfo = appleLoginObject.getUserInfo(AppleLoginRequest(identityToken = "token"))

        assertEquals("old@privaterelay.appleid.com", userInfo.email)
    }

    @Test
    fun `email도 없고 기존 회원도 없으면 예외를 던진다`() {
        every { verifier.verify("token") } returns payload(email = null)
        every { memberRepository.findByProviderAndProviderId(LoginType.APPLE.name, sub) } returns null

        assertThrows<LoginException> {
            appleLoginObject.getUserInfo(AppleLoginRequest(identityToken = "token"))
        }
    }

    @Test
    fun `이메일이 바뀌어도 sub로 같은 회원을 찾는다`() {
        val existing = member(email = "old@privaterelay.appleid.com", providerId = sub)
        every { memberRepository.findByProviderAndProviderId(LoginType.APPLE.name, sub) } returns existing

        val found = appleLoginObject.getMember(
            UserInfo(provider = LoginType.APPLE.name, email = "new@example.com", providerId = sub)
        )

        assertEquals(existing, found)
        verify(exactly = 0) { memberObject.signIn(any()) }
    }

    @Test
    fun `providerId가 없던 기존 회원은 조회 시 sub를 채워준다`() {
        val existing = member(email = "old@privaterelay.appleid.com", providerId = null)
        every { memberRepository.findByProviderAndProviderId(LoginType.APPLE.name, sub) } returns null
        every { memberRepository.findByEmail("old@privaterelay.appleid.com") } returns existing

        appleLoginObject.getMember(
            UserInfo(
                provider = LoginType.APPLE.name,
                email = "old@privaterelay.appleid.com",
                providerId = sub
            )
        )

        assertEquals(sub, existing.providerId)
        verify { memberRepository.save(existing) }
    }

    @Test
    fun `기존 회원이 없으면 신규 가입시킨다`() {
        val userInfo = UserInfo(provider = LoginType.APPLE.name, email = "new@example.com", providerId = sub)
        every { memberRepository.findByProviderAndProviderId(LoginType.APPLE.name, sub) } returns null
        every { memberRepository.findByEmail("new@example.com") } returns null
        every { memberObject.signIn(userInfo) } returns member(email = "new@example.com", providerId = sub)

        appleLoginObject.getMember(userInfo)

        verify { memberObject.signIn(userInfo) }
    }

    private fun payload(email: String?) = AppleIdTokenPayload(
        sub = sub,
        email = email,
        emailVerified = true,
        isPrivateEmail = true,
    )

    private fun member(email: String, providerId: String? = null) = Member(
        email = email,
        provider = LoginType.APPLE.name,
        providerId = providerId,
    )
}
