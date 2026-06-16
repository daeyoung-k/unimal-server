package com.unimal.admin.domain.adminmember

import com.unimal.admin.domain.adminmember.enums.AdminRole
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminMemberRoleTest {

    @Test
    fun `admin member role keeps member and role`() {
        val adminMember = AdminMember(
            loginId = "admin",
            password = "encoded-password",
            name = "관리자"
        )

        val adminMemberRole = AdminMemberRole(
            adminMember = adminMember,
            role = AdminRole.SUPER_ADMIN
        )

        assertEquals(adminMember, adminMemberRole.adminMember)
        assertEquals(AdminRole.SUPER_ADMIN, adminMemberRole.role)
    }
}
