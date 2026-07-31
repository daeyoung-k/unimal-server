package com.unimal.user.service.login

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
import com.unimal.common.enums.UserStatus
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.token.TokenManager
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.service.login.apple.AppleAuthClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginServiceProviderIndependenceTest {

    private val kakaoLoginObject: LoginInterface = mockk()
    private val naverLoginObject: LoginInterface = mockk()
    private val googleLoginObject: LoginInterface = mockk()
    private val appleLoginObject: LoginInterface = mockk()
    private val manualLoginObject: LoginInterface = mockk()
    private val appleAuthClient: AppleAuthClient = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val memberObject: MemberObject = mockk()
    private val memberKafkaTopic: MemberKafkaTopic = mockk(relaxed = true)
    private val memberRepository: MemberRepository = mockk()
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
        memberRepository = memberRepository
    )

    @Test
    fun `social login for an existing manual member issues token with stored provider`() {
        val request = NaverLoginRequest(
            email = "leaf@unimal.co.kr",
            name = "김리프",
            nickname = "리프",
            profileImage = null
        )
        val userInfo = UserInfo(
            provider = LoginType.NAVER.name,
            email = "leaf@unimal.co.kr",
            name = "김리프",
            nickname = "리프"
        )
        val member = Member(
            email = "leaf@unimal.co.kr",
            provider = LoginType.MANUAL.name,
            nickname = "리프",
            tel = "01012345678",
            status = UserStatus.ACTIVE
        )
        val token = JwtTokenDTO(
            email = "leaf@unimal.co.kr",
            accessToken = "access",
            refreshToken = "refresh",
            provider = LoginType.MANUAL.name
        )

        every { naverLoginObject.getUserInfo(request) } returns userInfo
        every { naverLoginObject.getMember(userInfo) } returns member
        every {
            tokenManager.createJwtToken(
                email = "leaf@unimal.co.kr",
                nickname = "리프",
                provider = LoginType.MANUAL,
                role = emptyList()
            )
        } returns token

        val result = loginService.login(request)

        assertEquals(LoginType.MANUAL.name, result?.provider)
        verify(exactly = 1) {
            tokenManager.createJwtToken(
                email = "leaf@unimal.co.kr",
                nickname = "리프",
                provider = LoginType.MANUAL,
                role = emptyList()
            )
        }
    }

    @Test
    fun `logout finds member by email even when token provider differs`() {
        val commonUserInfo = CommonUserInfo(
            email = "leaf@unimal.co.kr",
            nickname = "리프",
            roles = listOf("USER"),
            provider = LoginType.NAVER.name,
            tokenType = TokenType.REFRESH
        )
        val member = Member(
            email = "leaf@unimal.co.kr",
            provider = LoginType.MANUAL.name,
            nickname = "리프"
        )

        every { memberObject.getEmailProviderMember("leaf@unimal.co.kr", LoginType.NAVER) } returns member

        loginService.logout(commonUserInfo)

        verify(exactly = 1) { memberObject.getEmailProviderMember("leaf@unimal.co.kr", LoginType.NAVER) }
        verify(exactly = 1) { tokenManager.deleteCacheToken("leaf@unimal.co.kr") }
        verify(exactly = 1) { tokenManager.revokDbToken("leaf@unimal.co.kr") }
    }
}
