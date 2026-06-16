package com.unimal.admin.controller.view.appmember

import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.service.appmember.AppMemberSearchCondition
import com.unimal.admin.service.appmember.AppMemberProviderCount
import com.unimal.admin.service.appmember.AppMemberService
import com.unimal.admin.service.appmember.AppMemberSort
import com.unimal.common.enums.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.ui.ExtendedModelMap

class AppMemberViewControllerTest {

    private val appMemberService = mockk<AppMemberService>()
    private val appMemberViewController = AppMemberViewController(appMemberService)
    private val providerCounts = listOf(
        AppMemberProviderCount(value = "KAKAO", label = "카카오", count = 1L),
        AppMemberProviderCount(value = "NAVER", label = "네이버", count = 0L),
        AppMemberProviderCount(value = "GOOGLE", label = "구글", count = 0L),
        AppMemberProviderCount(value = "MANUAL", label = "일반", count = 0L)
    )

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

        every {
            appMemberService.getMembers(
                page = 1,
                size = 10,
                condition = AppMemberSearchCondition()
            )
        } returns membersPage
        every { appMemberService.getProviderCounts(AppMemberSearchCondition()) } returns providerCounts

        val viewName = appMemberViewController.list(page = 1, size = 10, model = model)

        assertEquals("appmember/list", viewName)
        assertSame(membersPage, model.asMap()["membersPage"])
        assertEquals(providerCounts, model.asMap()["providerCounts"])
        assertEquals(1, model.asMap()["page"])
        assertEquals(10, model.asMap()["size"])
    }

    @Test
    fun `member list view keeps normalized filter state`() {
        val membersPage = PageImpl(emptyList<AppMember>(), PageRequest.of(0, 20), 0)
        val expectedCondition = AppMemberSearchCondition(
            status = UserStatus.ACTIVE,
            provider = "KAKAO",
            keyword = "leaf",
            sort = AppMemberSort.OLDEST
        )
        val model = ExtendedModelMap()

        every {
            appMemberService.getMembers(
                page = 0,
                size = 20,
                condition = expectedCondition
            )
        } returns membersPage
        every { appMemberService.getProviderCounts(expectedCondition) } returns providerCounts

        val viewName = appMemberViewController.list(
            page = 0,
            size = 20,
            status = "ACTIVE",
            provider = "kakao",
            keyword = "  leaf  ",
            sort = "oldest",
            model = model
        )

        assertEquals("appmember/list", viewName)
        verify {
            appMemberService.getMembers(
                page = 0,
                size = 20,
                condition = expectedCondition
            )
        }
        assertEquals(expectedCondition, model.asMap()["filter"])
        assertEquals(providerCounts, model.asMap()["providerCounts"])
        assertEquals(AppMemberSort.entries, model.asMap()["sortOptions"])
        assertEquals(AppMemberSearchCondition.providerOptions, model.asMap()["providerOptions"])
    }

    @Test
    fun `blank filter params are treated as default filters`() {
        val membersPage = PageImpl(emptyList<AppMember>(), PageRequest.of(0, 20), 0)
        val expectedCondition = AppMemberSearchCondition()
        val model = ExtendedModelMap()

        every {
            appMemberService.getMembers(
                page = 0,
                size = 20,
                condition = expectedCondition
            )
        } returns membersPage
        every { appMemberService.getProviderCounts(expectedCondition) } returns providerCounts

        val viewName = appMemberViewController.list(
            page = 0,
            size = 20,
            status = "",
            provider = "",
            keyword = "   ",
            sort = "",
            model = model
        )

        assertEquals("appmember/list", viewName)
        verify {
            appMemberService.getMembers(
                page = 0,
                size = 20,
                condition = expectedCondition
            )
        }
        assertEquals(expectedCondition, model.asMap()["filter"])
    }
}
