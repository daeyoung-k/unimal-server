package com.unimal.admin.config

import com.unimal.admin.service.appmember.AppMemberSearchCondition
import com.unimal.admin.service.appmember.AppMemberSort
import com.unimal.common.enums.UserStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `login page is public`() {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Stomap Admin")))
    }

    @Test
    fun `anonymous user is redirected to login for members`() {
        mockMvc.perform(get("/members"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login"))
    }

    @Test
    fun `authenticated admin can view member list`() {
        mockMvc.perform(
            get("/members")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("회원 관리")))
    }

    @Test
    fun `authenticated admin can view member list filters`() {
        val expectedCondition = AppMemberSearchCondition(
            status = UserStatus.ACTIVE,
            provider = "KAKAO",
            keyword = "leaf",
            sort = AppMemberSort.OLDEST
        )

        mockMvc.perform(
            get("/members")
                .param("status", "ACTIVE")
                .param("provider", "kakao")
                .param("keyword", "  leaf  ")
                .param("sort", "oldest")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(model().attribute("filter", expectedCondition))
            .andExpect(model().attributeExists("providerCounts"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"summary-provider\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"filters\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"keyword\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("가입 방식")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("오래된순")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("카카오")))
    }

    @Test
    fun `authenticated admin sees logout inside sidebar`() {
        mockMvc.perform(
            get("/members")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"sidebar-footer\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"sidebar-logout\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("로그아웃")))
    }

    @Test
    fun `authenticated admin can view member detail moderation page`() {
        mockMvc.perform(
            get("/members/1")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("회원 상세")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("프로필 이미지 초기화")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("소개글 숨김")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("회원 차단")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("운영 로그")))
    }

    @Test
    @Transactional
    fun `profile image reset log renders previous image preview`() {
        mockMvc.perform(
            post("/members/1/actions/reset-profile-image")
                .param("reason", "부적절한 프로필 이미지")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection)

        mockMvc.perform(
            get("/members/1")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("운영 조치 전 이미지")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"log-image-preview\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("src=\"https://cdn.unimal.co.kr/profile/leaf.png\"")))
    }

    @Test
    fun `admin role cannot access admin member management`() {
        mockMvc.perform(
            get("/admin-members")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isForbidden)
    }
}
