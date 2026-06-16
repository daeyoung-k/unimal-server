package com.unimal.admin.config

import com.unimal.admin.service.appmember.AppMemberSearchCondition
import com.unimal.admin.service.appmember.AppMemberSort
import com.unimal.common.enums.UserStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
    fun `admin role cannot access admin member management`() {
        mockMvc.perform(
            get("/admin-members")
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isForbidden)
    }
}
