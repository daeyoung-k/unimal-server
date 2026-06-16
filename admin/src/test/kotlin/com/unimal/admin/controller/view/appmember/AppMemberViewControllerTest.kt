package com.unimal.admin.controller.view.appmember

import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.service.appmember.AppMemberService
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.ui.ExtendedModelMap

class AppMemberViewControllerTest {

    private val appMemberService = mockk<AppMemberService>()
    private val appMemberViewController = AppMemberViewController(appMemberService)

    @Test
    fun `member list view exposes members page`() {
        val membersPage = PageImpl(
            listOf(
                AppMember(
                    email = "user@unimal.co.kr",
                    nickname = "스토맵유저",
                    provider = "MANUAL"
                )
            ),
            PageRequest.of(1, 10),
            11
        )
        val model = ExtendedModelMap()

        every { appMemberService.getMembers(page = 1, size = 10) } returns membersPage

        val viewName = appMemberViewController.list(page = 1, size = 10, model = model)

        assertEquals("appmember/list", viewName)
        assertSame(membersPage, model.asMap()["membersPage"])
        assertEquals(1, model.asMap()["page"])
        assertEquals(10, model.asMap()["size"])
    }
}
