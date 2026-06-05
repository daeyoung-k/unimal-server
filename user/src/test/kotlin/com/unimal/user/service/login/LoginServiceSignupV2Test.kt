package com.unimal.user.service.login

import com.unimal.user.controller.request.SignupV2Request
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.token.TokenManager
import com.unimal.webcommon.exception.DuplicatedException
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.TelNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LoginServiceSignupV2Test {

    private val kakaoLoginObject: LoginInterface = mockk()
    private val naverLoginObject: LoginInterface = mockk()
    private val googleLoginObject: LoginInterface = mockk()
    private val manualLoginObject: ManualLoginObject = mockk()
    private val tokenManager: TokenManager = mockk()
    private val memberObject: MemberObject = mockk()
    private val memberKafkaTopic: MemberKafkaTopic = mockk()
    private val memberRepository: MemberRepository = mockk()

    private val loginService = LoginService(
        kakaoLoginObject = kakaoLoginObject,
        naverLoginObject = naverLoginObject,
        googleLoginObject = googleLoginObject,
        manualLoginObject = manualLoginObject,
        tokenManager = tokenManager,
        memberObject = memberObject,
        memberKafkaTopic = memberKafkaTopic,
        memberRepository = memberRepository,
    )

    private val validRequest = SignupV2Request(
        nickname = "테스터",
        email = "test@test.com",
        password = "Test1234!",
        checkPassword = "Test1234!",
    )

    @Test
    fun `이메일 중복 시 DuplicatedException 발생`() {
        every { memberRepository.findByEmail("test@test.com") } returns mockk()

        assertThrows<DuplicatedException> {
            loginService.signupV2(validRequest)
        }
    }

    @Test
    fun `비밀번호 불일치 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null

        val request = validRequest.copy(checkPassword = "WrongPass1!")
        assertThrows<LoginException> {
            loginService.signupV2(request)
        }
    }

    @Test
    fun `비밀번호 형식 불일치 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns false

        val request = validRequest.copy(password = "simple", checkPassword = "simple")
        assertThrows<LoginException> {
            loginService.signupV2(request)
        }
    }

    @Test
    fun `이메일 인증 미완료 시 LoginException 발생`() {
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns true
        every { manualLoginObject.emailSuccessCheck("test@test.com") } returns false

        assertThrows<LoginException> {
            loginService.signupV2(validRequest)
        }
    }

    @Test
    fun `정상 가입 시 TelNotFoundException 발생 (code=1009, data=email)`() {
        val savedMember: com.unimal.user.domain.member.Member = mockk {
            every { email } returns "test@test.com"
        }
        every { memberRepository.findByEmail(any()) } returns null
        every { memberObject.passwordFormatCheck(any()) } returns true
        every { manualLoginObject.emailSuccessCheck("test@test.com") } returns true
        every { memberObject.signIn(any()) } returns savedMember

        val ex = assertThrows<TelNotFoundException> {
            loginService.signupV2(validRequest)
        }
        assertEquals("test@test.com", ex.data)
        assertEquals(1009, ex.code)
    }
}
