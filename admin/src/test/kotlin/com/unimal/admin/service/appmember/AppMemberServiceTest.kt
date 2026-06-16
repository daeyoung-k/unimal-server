package com.unimal.admin.service.appmember

import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.domain.appmember.AppMemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class AppMemberServiceTest {

    private val appMemberRepository = mockk<AppMemberRepository>()
    private val appMemberService = AppMemberService(appMemberRepository)

    @Test
    fun `members are loaded newest first with paging`() {
        val member = AppMember(
            email = "user@unimal.co.kr",
            nickname = "스토맵유저",
            provider = "MANUAL"
        )
        val pageableSlot = slot<Pageable>()

        every { appMemberRepository.findAll(any<Pageable>()) } returns PageImpl(listOf(member))

        val members = appMemberService.getMembers(page = 0, size = 20)

        verify { appMemberRepository.findAll(capture(pageableSlot)) }
        assertEquals(listOf(member), members.content)
        assertEquals(0, pageableSlot.captured.pageNumber)
        assertEquals(20, pageableSlot.captured.pageSize)
        assertEquals(Sort.Direction.DESC, pageableSlot.captured.sort.getOrderFor("createdAt")?.direction)
    }
}
