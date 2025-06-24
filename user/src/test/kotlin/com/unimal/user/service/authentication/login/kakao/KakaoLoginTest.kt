package com.unimal.user.service.authentication.login.kakao

import com.unimal.webcommon.exception.LoginException
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.Role
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.login.KakaoLoginObject
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate

@SpringBootTest
@ActiveProfiles("local")
class KakaoLoginTest {
    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val memberRoleRepository: MemberRoleRepository = mockk(relaxed = true)
    private val roleRepository: RoleRepository = mockk(relaxed = true)

    private val kakaoLoginObject = KakaoLoginObject()
    private val memberObject = MemberObject(memberRepository, memberRoleRepository, roleRepository)

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

        // 실제 API 호출 구조
        val fakeResponseBody = """
            {
                "id": 1234567890,
                "connected_at": "2025-03-10T14:50:38Z",
                "properties": {}, 
                "kakao_account": {
                    }, 
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
        assertThrows(LoginException::class.java) { kakaoLoginObject.getUserInfo(fakeToken) }

    }

    @Test
    fun `getMember - Member 를 반환한다` () {
        // given
        val email = "test@kakao.com"
        every { memberRepository.findByEmailAndProvider(email, LoginType.KAKAO.name) } returns Member(email = email, provider = LoginType.KAKAO.name)

        // when
        val result = memberObject.getMember(email, LoginType.KAKAO)

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
        val result = memberObject.getMember(email, LoginType.KAKAO)

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
        every { memberRepository.save(userInfo.toEntity()) } returns userInfo.toEntity()
        every { roleRepository.findByName(MemberRoleCode.USER.name) } returns Role(name = MemberRoleCode.USER.name)
        every { memberRoleRepository.save(userInfo.toEntity().getMemberRole(Role(name = MemberRoleCode.USER.name))) }

        // when
        val result = memberObject.signIn(userInfo)

        // then
        assertEquals("test@kakao.com", result.email)
        assertEquals("KAKAO", result.provider)
    }

    @Test
    fun createJwtToken() {}
}