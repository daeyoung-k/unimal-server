package com.unimal.admin.service.appmember

import com.unimal.admin.domain.adminmember.AdminMemberRepository
import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.domain.appmember.AppMemberRepository
import com.unimal.admin.domain.appmember.actionlog.AppMemberActionLogRepository
import com.unimal.common.enums.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

class AppMemberServiceTest {

    private val appMemberRepository = mockk<AppMemberRepository>()
    private val adminMemberRepository = mockk<AdminMemberRepository>()
    private val appMemberActionLogRepository = mockk<AppMemberActionLogRepository>()
    private val appMemberService = AppMemberService(
        appMemberRepository,
        adminMemberRepository,
        appMemberActionLogRepository
    )

    @Test
    fun `members are loaded newest first with paging`() {
        val member = AppMember(
            email = "user@unimal.co.kr",
            nickname = "스토맵유저",
            provider = "MANUAL"
        )
        val pageableSlot = slot<Pageable>()

        every {
            appMemberRepository.findAll(any<Specification<AppMember>>(), any<Pageable>())
        } returns PageImpl(listOf(member))

        val members = appMemberService.getMembers(page = 0, size = 20)

        verify {
            appMemberRepository.findAll(any<Specification<AppMember>>(), capture(pageableSlot))
        }
        assertEquals(listOf(member), members.content)
        assertEquals(0, pageableSlot.captured.pageNumber)
        assertEquals(20, pageableSlot.captured.pageSize)
        assertEquals(Sort.Direction.DESC, pageableSlot.captured.sort.getOrderFor("createdAt")?.direction)
    }

    @Test
    fun `members can be sorted oldest first`() {
        val pageableSlot = slot<Pageable>()

        every {
            appMemberRepository.findAll(any<Specification<AppMember>>(), any<Pageable>())
        } returns PageImpl(emptyList())

        appMemberService.getMembers(
            page = 0,
            size = 20,
            condition = AppMemberSearchCondition(sort = AppMemberSort.OLDEST)
        )

        verify {
            appMemberRepository.findAll(any<Specification<AppMember>>(), capture(pageableSlot))
        }
        assertEquals(Sort.Direction.ASC, pageableSlot.captured.sort.getOrderFor("createdAt")?.direction)
    }

    @Test
    fun `filter condition is normalized before querying`() {
        val pageableSlot = slot<Pageable>()

        every {
            appMemberRepository.findAll(any<Specification<AppMember>>(), any<Pageable>())
        } returns PageImpl(emptyList())

        appMemberService.getMembers(
            page = -10,
            size = 999,
            condition = AppMemberSearchCondition(
                status = UserStatus.ACTIVE,
                provider = "kakao",
                keyword = "  leaf  ",
                sort = AppMemberSort.UPDATED
            )
        )

        verify {
            appMemberRepository.findAll(any<Specification<AppMember>>(), capture(pageableSlot))
        }
        assertEquals(0, pageableSlot.captured.pageNumber)
        assertEquals(100, pageableSlot.captured.pageSize)
        assertEquals(Sort.Direction.DESC, pageableSlot.captured.sort.getOrderFor("updatedAt")?.direction)
    }
}
