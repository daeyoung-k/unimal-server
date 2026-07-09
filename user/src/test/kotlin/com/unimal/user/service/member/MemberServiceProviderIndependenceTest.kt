package com.unimal.user.service.member

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
import com.unimal.user.controller.request.InfoUpdateRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.file.FileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MemberServiceProviderIndependenceTest {

    private val memberObject: MemberObject = mockk()
    private val fileService: FileService = mockk()
    private val memberKafkaTopic: MemberKafkaTopic = mockk(relaxed = true)
    private val memberRepository: MemberRepository = mockk()
    private val memberService = MemberService(
        memberObject = memberObject,
        fileService = fileService,
        memberKafkaTopic = memberKafkaTopic,
        memberRepository = memberRepository,
        fileBaseUrl = "https://cdn.unimal.co.kr"
    )

    @Test
    fun `member info is loaded by email even when token provider differs from stored provider`() {
        val commonUserInfo = CommonUserInfo(
            email = "leaf@unimal.co.kr",
            nickname = "리프",
            roles = listOf("USER"),
            provider = "NAVER",
            tokenType = TokenType.ACCESS
        )
        val member = Member(
            email = "leaf@unimal.co.kr",
            provider = "MANUAL",
            nickname = "리프",
            name = "김리프",
            tel = "01012345678",
            profileImage = "https://cdn.unimal.co.kr/profile/leaf.png"
        )

        every { memberRepository.findByEmail("leaf@unimal.co.kr") } returns member

        val result = memberService.getMemberInfo(commonUserInfo)

        assertEquals("leaf@unimal.co.kr", result.email)
        assertEquals("MANUAL", result.provider)
        assertEquals("리프", result.nickname)
        verify(exactly = 1) { memberRepository.findByEmail("leaf@unimal.co.kr") }
    }

    @Test
    fun `member update uses email lookup when token provider differs from stored provider`() {
        val commonUserInfo = CommonUserInfo(
            email = "leaf@unimal.co.kr",
            nickname = "리프",
            roles = listOf("USER"),
            provider = "NAVER",
            tokenType = TokenType.ACCESS
        )
        val member = Member(
            email = "leaf@unimal.co.kr",
            provider = "MANUAL",
            nickname = "리프",
            name = "김리프"
        )

        every { memberObject.getEmailProviderMember("leaf@unimal.co.kr", any()) } returns member

        memberService.updateMemberInfo(
            commonUserInfo = commonUserInfo,
            infoUpdateRequest = InfoUpdateRequest(
                name = "김리프2",
                nickname = null,
                tel = null,
                introduction = null,
                birthday = null,
                gender = null
            )
        )

        assertEquals("김리프2", member.name)
        verify(exactly = 1) { memberObject.getEmailProviderMember("leaf@unimal.co.kr", any()) }
    }
}
