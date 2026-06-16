package com.unimal.admin.service.appmember

import com.unimal.admin.domain.appmember.actionlog.AppMemberActionType
import com.unimal.common.enums.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AppMemberServiceIntegrationTest @Autowired constructor(
    private val appMemberService: AppMemberService
) {

    @Test
    fun `members are filtered by status provider and keyword`() {
        val members = appMemberService.getMembers(
            page = 0,
            size = 20,
            condition = AppMemberSearchCondition(
                status = UserStatus.ACTIVE,
                provider = "KAKAO",
                keyword = "leaf",
                sort = AppMemberSort.LATEST
            )
        )

        assertEquals(listOf("leaf@unimal.co.kr"), members.content.map { it.email })
    }

    @Test
    fun `members can be sorted oldest first`() {
        val members = appMemberService.getMembers(
            page = 0,
            size = 20,
            condition = AppMemberSearchCondition(sort = AppMemberSort.OLDEST)
        )

        assertEquals(
            listOf("river@unimal.co.kr", "leaf@unimal.co.kr"),
            members.content.map { it.email }
        )
    }

    @Test
    fun `member detail contains profile moderation fields`() {
        val member = appMemberService.getMember(1)

        assertEquals("leaf@unimal.co.kr", member.email)
        assertEquals("https://cdn.unimal.co.kr/profile/leaf.png", member.profileImage)
        assertEquals("안녕하세요. 리프입니다.", member.introduction)
    }

    @Test
    fun `profile image reset updates member and records action log`() {
        appMemberService.resetProfileImage(
            memberId = 1,
            adminLoginId = "admin",
            reason = "부적절한 프로필 이미지"
        )

        val member = appMemberService.getMember(1)
        val logs = appMemberService.getActionLogs(1)

        assertNull(member.profileImage)
        assertEquals(AppMemberActionType.PROFILE_IMAGE_RESET, logs.first().actionType)
        assertEquals(1L, logs.first().adminMemberId)
        assertEquals("admin", logs.first().adminLoginId)
        assertEquals(1L, logs.first().targetMemberId)
        assertEquals("leaf@unimal.co.kr", logs.first().targetMemberEmail)
        assertEquals("부적절한 프로필 이미지", logs.first().reason)
        assertEquals("https://cdn.unimal.co.kr/profile/leaf.png", logs.first().beforeValue)
        assertNull(logs.first().afterValue)
    }

    @Test
    fun `introduction hide updates member and records action log`() {
        appMemberService.hideIntroduction(
            memberId = 1,
            adminLoginId = "admin",
            reason = "부적절한 소개글"
        )

        val member = appMemberService.getMember(1)
        val logs = appMemberService.getActionLogs(1)

        assertNull(member.introduction)
        assertEquals(AppMemberActionType.INTRODUCTION_HIDE, logs.first().actionType)
        assertEquals("안녕하세요. 리프입니다.", logs.first().beforeValue)
        assertNull(logs.first().afterValue)
    }

    @Test
    fun `member can be blocked and unblocked with action logs`() {
        appMemberService.blockMember(
            memberId = 1,
            adminLoginId = "admin",
            reason = "반복 신고 누적"
        )
        appMemberService.unblockMember(
            memberId = 1,
            adminLoginId = "admin",
            reason = "오조치 복구"
        )

        val member = appMemberService.getMember(1)
        val logs = appMemberService.getActionLogs(1)

        assertEquals(UserStatus.ACTIVE, member.status)
        assertEquals(AppMemberActionType.MEMBER_UNBLOCK, logs[0].actionType)
        assertEquals(UserStatus.BLOCK.name, logs[0].beforeValue)
        assertEquals(UserStatus.ACTIVE.name, logs[0].afterValue)
        assertEquals(AppMemberActionType.MEMBER_BLOCK, logs[1].actionType)
        assertEquals(UserStatus.ACTIVE.name, logs[1].beforeValue)
        assertEquals(UserStatus.BLOCK.name, logs[1].afterValue)
    }

    @Test
    fun `provider counts follow current member filters`() {
        val counts = appMemberService.getProviderCounts(
            AppMemberSearchCondition(status = UserStatus.ACTIVE)
        )

        assertEquals(
            mapOf(
                "KAKAO" to 1L,
                "NAVER" to 0L,
                "GOOGLE" to 0L,
                "MANUAL" to 0L
            ),
            counts.associate { it.value to it.count }
        )
    }

    @Test
    fun `provider counts are constrained by selected provider`() {
        val counts = appMemberService.getProviderCounts(
            AppMemberSearchCondition(provider = "NAVER")
        )

        assertEquals(
            mapOf(
                "KAKAO" to 0L,
                "NAVER" to 1L,
                "GOOGLE" to 0L,
                "MANUAL" to 0L
            ),
            counts.associate { it.value to it.count }
        )
    }
}
