package com.unimal.admin.controller.view

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * 어드민 화면이 실제로 렌더되는지 확인한다.
 *
 * **뷰 컨트롤러 단위 테스트로는 이걸 못 잡는다.** 그쪽은 반환된 뷰 이름 문자열만
 * 검사하므로, 템플릿 안의 fragment 참조가 틀렸거나 표현식에 오타가 있어도 통과한다.
 * 실제로는 화면을 열었을 때 500 이 난다.
 *
 * 공통 레이아웃(`fragments/layout`)과 `static/css/admin.css` 로 스타일을 모으면서
 * 모든 템플릿의 `<head>` 와 사이드바를 갈아엎었기 때문에, 한 곳만 어긋나도 전체
 * 화면이 죽는다. 그래서 여기서 한 번에 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminViewRenderingTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {

    @Test
    @WithMockUser(username = "admin", roles = ["SUPER_ADMIN"])
    fun `공지 목록 화면이 렌더된다`() {
        mockMvc.get("/notices")
            .andExpect {
                status { isOk() }
                view { name("notice/list") }
                // 사이드바 fragment 가 붙었는지 — 레이아웃이 안 끼면 여기가 빈다.
                content { string(containsString("Stomap Admin")) }
                content { string(containsString("공지 관리")) }
                content { string(containsString("/css/admin.css")) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["SUPER_ADMIN"])
    fun `공지 작성 화면이 렌더된다`() {
        // notice 가 null 인 분기다. 수정 화면과 같은 템플릿을 쓰므로
        // 여기가 통과하면 삼항 표현식이 최소한 파싱은 된 것이다.
        mockMvc.get("/notices/new")
            .andExpect {
                status { isOk() }
                view { name("notice/form") }
                content { string(containsString("공지 등록")) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["SUPER_ADMIN"])
    fun `회원 목록 화면이 렌더된다`() {
        // 기존 화면이 레이아웃 전환으로 깨지지 않았는지 확인한다.
        mockMvc.get("/members")
            .andExpect {
                status { isOk() }
                view { name("appmember/list") }
                content { string(containsString("회원 관리")) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["SUPER_ADMIN"])
    fun `회원 상세 화면이 렌더된다`() {
        // data.sql 의 1번 회원.
        mockMvc.get("/members/1")
            .andExpect {
                status { isOk() }
                view { name("appmember/detail") }
                content { string(containsString("회원 상세")) }
            }
    }

    @Test
    fun `로그인 화면은 인증 없이 열리고 공통 CSS 를 참조한다`() {
        mockMvc.get("/login")
            .andExpect {
                status { isOk() }
                content { string(containsString("/css/admin.css")) }
            }
    }

    @Test
    fun `공통 CSS 는 인증 없이 받을 수 있다`() {
        // SecurityConfig 에서 /css/** 를 열어두지 않으면 로그인 페이지로 리다이렉트되어
        // 로그인 화면이 스타일 없이 뜬다. 브라우저로 열기 전에는 알아채기 어렵다.
        mockMvc.get("/css/admin.css")
            .andExpect {
                status { isOk() }
            }
    }
}
