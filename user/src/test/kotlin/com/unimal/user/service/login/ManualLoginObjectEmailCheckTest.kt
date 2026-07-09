package com.unimal.user.service.login

import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.service.member.MemberObject
import com.unimal.user.utils.RedisCacheManager
import com.unimal.webcommon.exception.LoginException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ManualLoginObjectEmailCheckTest {

    private val redisCacheManager: RedisCacheManager = mockk()
    private val memberObject: MemberObject = mockk()
    private val memberRepository: MemberRepository = mockk()

    private val manualLoginObject = ManualLoginObject(
        memberObject = memberObject,
        redisCacheManager = redisCacheManager,
        memberRepository = memberRepository
    )

    @Test
    fun `이메일 인증 완료된 경우 true 반환`() {
        every { redisCacheManager.getCache("test@test.com:auth-code") } returns "SUCCESS"

        assertTrue(manualLoginObject.emailSuccessCheck("test@test.com"))
    }

    @Test
    fun `이메일 인증 미완료 시 false 반환`() {
        every { redisCacheManager.getCache("test@test.com:auth-code") } returns null

        assertFalse(manualLoginObject.emailSuccessCheck("test@test.com"))
    }

    @Test
    fun `비밀번호가 없는 소셜 계정으로 수동 로그인 시 LoginException 발생`() {
        every { memberRepository.findByEmail("social@test.com") } returns com.unimal.user.domain.member.Member(
            email = "social@test.com",
            provider = "NAVER",
            password = null
        )

        assertThrows<LoginException> {
            manualLoginObject.getMember(
                com.unimal.user.service.login.dto.UserInfo(
                    provider = "MANUAL",
                    email = "social@test.com",
                    password = "Password123!"
                )
            )
        }
    }
}
