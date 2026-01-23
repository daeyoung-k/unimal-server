package com.unimal.user.service.authentication.login.kakao

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.Role
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.login.KakaoLoginObject
import com.unimal.user.service.token.TokenManager
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.utils.RedisCacheManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.client.RestTemplate

class KakaoLoginTest {
    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val memberRoleRepository: MemberRoleRepository = mockk(relaxed = true)
    private val roleRepository: RoleRepository = mockk(relaxed = true)
    private val memberKafkaTopic: MemberKafkaTopic = mockk(relaxed = true)
    private val passwordEncoder: BCryptPasswordEncoder = mockk(relaxed = true)
    private val redisCacheManager: RedisCacheManager = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)

    private val memberObject = MemberObject(memberRepository, memberRoleRepository, roleRepository, memberKafkaTopic, passwordEncoder, redisCacheManager)
    private val kakaoLoginObject = KakaoLoginObject(memberObject)

    // 테스트 실행 전 모킹 초기화
    @AfterEach
    fun tearDown() = unmockkAll()


    @Test
    fun `getInfo - UserInfo 를 반환한다`() {
        // given
        val fakeToken = "Test_Kakao_Access_Token"

        // 실제 API 호출 구조
        val fakeResponseBody = """
            {
                "id": 1234567890,
                "connected_at": "2025-03-10T14:50:38Z",
                "properties": {"nickname": "테스트"}, 
                "kakao_account": {
                    "profile_nickname_needs_agreement": false,
                    "profile": {
                        "nickname": "테스트",
                        "is_default_nickname": false
                    }, 
                    "has_email": true, 
                    "email_needs_agreement": false, 
                    "is_email_valid": true, 
                    "is_email_verified": true, 
                    "email": "test@kakao.com" 
                }
            }
        """.trimIndent()

        mockkConstructor(RestTemplate::class)
        every {
            anyConstructed<RestTemplate>().exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                any<HttpEntity<*>>(),
                String::class.java
            )
        } returns ResponseEntity(fakeResponseBody, HttpStatus.OK)

        // when
        val result: UserInfo = kakaoLoginObject.getUserInfo(fakeToken)

        // then
        assertEquals("KAKAO", result.provider)
        assertEquals("test@kakao.com", result.email)
        assertEquals("테스트", result.nickname)
    }

    @Test
    fun `getInfo - 유저가 없음으로 예외를 반환한다`() {
        // given
        val fakeToken = "Test_Kakao_Access_Token"

        // 실제 API 호출 구조 - 잘못된 JSON
        val fakeResponseBody = """
            {
                "id": 1234567890,
                "connected_at": "2025-03-10T14:50:38Z",
                "properties": {},
                "kakao_account": {
                    "email": ""
                }
            }
        """.trimIndent()

        mockkConstructor(RestTemplate::class)
        every {
            anyConstructed<RestTemplate>().exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                any<HttpEntity<*>>(),
                String::class.java
            )
        } returns ResponseEntity(fakeResponseBody, HttpStatus.OK)

        // when // then
        assertThrows(com.unimal.webcommon.exception.ApiCallException::class.java) { kakaoLoginObject.getUserInfo(fakeToken) }

    }

    @Test
    fun `getMember - Member 를 반환한다` () {
        // given
        val email = "test@kakao.com"
        every { memberRepository.findByEmailAndProvider(email, LoginType.KAKAO.name) } returns Member(email = email, provider = LoginType.KAKAO.name)

        // when
        val result = memberObject.getEmailProviderMember(email, LoginType.KAKAO)

        // then
        assertEquals("test@kakao.com", result?.email)
        assertEquals("KAKAO", result?.provider)
    }

    @Test
    fun `getMember - 유저가 없으면 null 을 반환한다` () {
        // given
        val email = "no-user@kakao.com"
        every { memberRepository.findByEmailAndProvider(email, LoginType.KAKAO.name) } returns null

        // when
        val result = memberObject.getEmailProviderMember(email, LoginType.KAKAO)

        // then
        assertNull(result)
    }

    @Test
    fun `signIn - Member 를 반환한다`() {
        // given
        val userInfo = UserInfo(
                provider = LoginType.KAKAO.name,
                email = "test@kakao.com"
            )
        val role = Role(name = MemberRoleCode.USER.name)
        val savedMember = userInfo.toEntity()

        every { memberRepository.save(any()) } returns savedMember
        every { roleRepository.findByName(MemberRoleCode.USER.name) } returns role
        every { memberRoleRepository.save(any()) } returns savedMember.getMemberRole(role)

        // when
        val result = memberObject.signIn(userInfo)

        // then
        assertEquals("test@kakao.com", result.email)
        assertEquals("KAKAO", result.provider)
    }

    @Test
    fun `createJwtToken - JwtTokenDTO를 반환한다`() {
        // given
        val email = "test@kakao.com"
        val nickname = "테스트"
        val provider = LoginType.KAKAO
        val roles = listOf(MemberRoleCode.USER.name)

        val expectedToken = JwtTokenDTO(
            email = email,
            accessToken = "fake.access.token",
            refreshToken = "fake.refresh.token",
            provider = provider.name
        )

        every {
            tokenManager.createJwtToken(email, nickname, provider, roles)
        } returns expectedToken

        // when
        val result = tokenManager.createJwtToken(email, nickname, provider, roles)

        // then
        assertEquals(email, result.email)
        assertEquals(provider.name, result.provider)
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        verify(exactly = 1) { tokenManager.createJwtToken(email, nickname, provider, roles) }
    }
}