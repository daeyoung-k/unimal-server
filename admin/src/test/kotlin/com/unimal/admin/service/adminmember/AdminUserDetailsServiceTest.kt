package com.unimal.admin.service.adminmember

import com.unimal.admin.domain.adminmember.AdminMember
import com.unimal.admin.domain.adminmember.AdminMemberRepository
import com.unimal.admin.domain.adminmember.enums.AdminMemberStatus
import com.unimal.admin.domain.adminmember.enums.AdminRole
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.springframework.security.core.userdetails.UsernameNotFoundException

class AdminUserDetailsServiceTest {

    private val adminMemberRepository = mockk<AdminMemberRepository>()
    private val adminUserDetailsService = AdminUserDetailsService(adminMemberRepository)

    @Test
    fun `active admin member is loaded with role authorities`() {
        val adminMember = AdminMember(
            loginId = "root",
            password = "encoded-password",
            name = "루트 관리자",
            status = AdminMemberStatus.ACTIVE
        )
        adminMember.addRole(AdminRole.SUPER_ADMIN)

        every { adminMemberRepository.findByLoginId("root") } returns adminMember

        val userDetails = adminUserDetailsService.loadUserByUsername("root")

        assertEquals("root", userDetails.username)
        assertEquals("encoded-password", userDetails.password)
        assertTrue(userDetails.authorities.any { it.authority == "ROLE_SUPER_ADMIN" })
    }

    @Test
    fun `inactive admin member cannot login`() {
        val adminMember = AdminMember(
            loginId = "inactive",
            password = "encoded-password",
            name = "비활성 관리자",
            status = AdminMemberStatus.INACTIVE
        )
        adminMember.addRole(AdminRole.ADMIN)

        every { adminMemberRepository.findByLoginId("inactive") } returns adminMember

        assertFailsWith<UsernameNotFoundException> {
            adminUserDetailsService.loadUserByUsername("inactive")
        }
    }
}
