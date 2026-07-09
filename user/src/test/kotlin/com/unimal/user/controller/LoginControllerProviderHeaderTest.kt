package com.unimal.user.controller

import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.service.authentication.AuthenticationService
import com.unimal.user.service.login.LoginService
import com.unimal.user.service.token.TokenService
import com.unimal.user.service.token.dto.JwtTokenDTO
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import kotlin.test.Test

class LoginControllerProviderHeaderTest {

    private val loginService: LoginService = mockk()
    private val tokenService: TokenService = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val loginController = LoginController(
        loginService = loginService,
        tokenService = tokenService,
        authenticationService = authenticationService
    )

    @Test
    fun `manual login response includes provider header`() {
        val request = ManualLoginRequest(
            email = "leaf@unimal.co.kr",
            password = "Password123!"
        )
        val response: HttpServletResponse = mockk(relaxed = true)
        val token = JwtTokenDTO(
            email = "leaf@unimal.co.kr",
            accessToken = "access",
            refreshToken = "refresh",
            provider = "MANUAL"
        )

        every { loginService.login(request) } returns token

        loginController.manualLogin(request, response)

        verify(exactly = 1) { response.setHeader("X-Unimal-Provider", "MANUAL") }
    }
}
