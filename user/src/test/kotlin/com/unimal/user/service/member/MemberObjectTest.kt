package com.unimal.user.service.member

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.service.authentication.login.enums.LoginType
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("local")
class MemberObjectTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val memberRoleRepository: MemberRoleRepository = mockk(relaxed = true)
    private val roleRepository: RoleRepository = mockk(relaxed = true)

    private val memberObject = MemberObject(memberRepository, memberRoleRepository, roleRepository)

    // 테스트 실행 전 모킹 초기화
    @AfterEach
    fun tearDown() = unmockkAll()

    // --- getMember ---
    @Test
    fun `멤버를 가져온다`() {
        // given
        val email = "test@test.com"
        val provider = LoginType.TEST
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
    fun `멤버를 가져오지 못한다`() {
        // Given
        val email = "test@test.com"
        val provider = LoginType.TEST
        every { memberRepository.findByEmailAndProvider(email, provider.name) } returns null

        // When
        val result = memberObject.getMember(email, provider)

        // Then
        assertNull(result)
    }

    // --- getMember ---
    @Test
    fun `회원가입에 성공한다`() {
        // Given

        // When

        // Then
    }


    @Test
    fun getMemberInfo() {
    }
}