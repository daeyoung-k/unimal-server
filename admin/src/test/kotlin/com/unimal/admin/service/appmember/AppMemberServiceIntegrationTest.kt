package com.unimal.admin.service.appmember

import com.unimal.common.enums.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
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
