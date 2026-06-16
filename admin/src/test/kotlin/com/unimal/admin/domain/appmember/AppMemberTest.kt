package com.unimal.admin.domain.appmember

import com.unimal.common.enums.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppMemberTest {

    @Test
    fun `app member maps app user fields for list view`() {
        val member = AppMember(
            email = "user@unimal.co.kr",
            nickname = "스토맵유저",
            provider = "MANUAL",
            status = UserStatus.ACTIVE
        )

        assertEquals("user@unimal.co.kr", member.email)
        assertEquals("스토맵유저", member.nickname)
        assertEquals("MANUAL", member.provider)
        assertEquals(UserStatus.ACTIVE, member.status)
        assertNull(member.tel)
    }
}
