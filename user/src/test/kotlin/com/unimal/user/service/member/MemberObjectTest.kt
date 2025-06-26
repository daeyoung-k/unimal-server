package com.unimal.user.service.member

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRole
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.Role
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.dto.MemberInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import com.unimal.webcommon.exception.LoginException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("local")
class MemberObjectTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val memberRoleRepository: MemberRoleRepository = mockk(relaxed = true)
    private val roleRepository: RoleRepository = mockk(relaxed = true)
    private val memberKafkaTopic: MemberKafkaTopic = mockk(relaxed = true)
    private val passwordEncoder: BCryptPasswordEncoder = mockk(relaxed = true)

    private val memberObject = MemberObject(
        memberRepository,
        memberRoleRepository,
        roleRepository,
        memberKafkaTopic,
        passwordEncoder
    )

    companion object {
        private val TEST_EMAIL = "test@test.com"
        private val TEST_PROVIDER = LoginType.TEST
        private val TEST_ROLE = MemberRoleCode.USER

    }

    // 테스트 실행 전 모킹 초기화
    @AfterEach
    fun tearDown() = unmockkAll()

    @Test
    fun `getMember - 멤버를 가져온다`() {
        // given
        val email = TEST_EMAIL
        val provider = TEST_PROVIDER
        val member = Member(email = email, provider = provider.name)

        every { memberRepository.findByEmailAndProvider(email, provider.name) } returns member

        // when
        val result = memberObject.getMember(email, provider)

        // then
        assertNotNull(result)
        assertEquals(member, result)
        assertEquals(email, result.email)
        assertEquals(provider.name, result.provider)
    }

    @Test
    fun `getMember - 멤버를 가져오지 못한다`() {
        // Given
        val email = TEST_EMAIL
        val provider = TEST_PROVIDER
        every { memberRepository.findByEmailAndProvider(email, provider.name) } returns null

        // When
        val result = memberObject.getMember(email, provider)

        // Then
        assertNull(result)
    }

    // --- getMember ---
    @Test
    fun `signIn - 회원가입에 성공한다`() {
        // Given
        val userInfo = mockk<UserInfo> {
            every { toEntity() } returns Member(
                email = TEST_EMAIL,
                provider = TEST_PROVIDER.name
            )
        }

        val member = userInfo.toEntity()
        val role = Role(TEST_ROLE.name)
        val memberRole = MemberRole(
            memberId = member,
            roleName = role
        )

        every { memberRepository.save(any()) } returns member
        every { roleRepository.findByName(TEST_ROLE.name) } returns role
        every { memberRoleRepository.save(any()) } returns memberRole

        // When
        val result = memberObject.signIn(userInfo)

        // Then
        assertEquals(member, result)
        assertEquals(TEST_EMAIL, result.email)
        assertEquals(TEST_PROVIDER.name, result.provider)
    }


    @Test
    fun `getMemberInfo - 존재하는 유저 정보를 가져온다`() {
        // Given
        val member = Member(
            email = TEST_EMAIL,
            provider = TEST_PROVIDER.name,
        )
        val memberInfo = MemberInfo(
            email = TEST_EMAIL,
            provider = TEST_PROVIDER.name
        )

        every { memberObject.getMember(TEST_EMAIL, TEST_PROVIDER) } returns member

        //When
        val result = memberObject.getMemberInfo(TEST_EMAIL, TEST_PROVIDER)

        //Then
        assertNotNull(result)
        assertEquals(memberInfo, result)
        assertEquals(TEST_EMAIL, result.email)
    }

    @Test
    fun `getMemberInfo - 존재하지 않는 유저를 반환하여 예외를 발생한다`() {
        //Given
        every { memberObject.getMember(TEST_EMAIL, TEST_PROVIDER) } returns null

        //When //Then
        assertThrows(LoginException::class.java) {memberObject.getMemberInfo(TEST_EMAIL, TEST_PROVIDER)}
    }


}