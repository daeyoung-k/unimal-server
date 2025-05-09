package com.unimal.user.service.authentication.login

import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.controller.request.GoogleLoginRequest
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.LoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import com.unimal.user.exception.UserNotFoundException
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.authentication.login.social.LoginInterface
import com.unimal.user.service.authentication.token.JwtProvider
import com.unimal.user.service.authentication.token.TokenManager
import com.unimal.user.service.authentication.token.dto.JwtTokenDTO
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLoginService") private val kakaoLoginService: LoginInterface,
    @Qualifier("NaverLoginService") private val naverLoginService: LoginInterface,
    @Qualifier("GoogleLoginService") private val googleLoginService: LoginInterface,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider,
    private val memberService: MemberService,
) {

    @Transactional
    fun login(loginRequest: LoginRequest): JwtTokenDTO? {

        val provider: LoginType = loginRequest.provider
        val userInfo: UserInfo = when (loginRequest) {
            is KakaoLoginRequest -> kakaoLoginService.getInfo(loginRequest.token)
            is NaverLoginRequest -> naverLoginService.getInfo(loginRequest)
            is GoogleLoginRequest -> googleLoginService.getInfo(loginRequest)
            else -> {
                throw LoginException(ErrorCode.LOGIN_NOT_SUPPORTED.message)
            }
        }

        val member = memberService.getMember(userInfo.email, provider) ?: memberService.signIn(userInfo)
        val roles = member.roles.map { it.roleName.name }

        val accessToken = createAccessJwtToken(member.email, provider, roles)
        tokenManager.saveCacheToken(member.email, accessToken)

        val refreshToken = createRefreshJwtToken(member.email, provider, roles)
        tokenManager.upsertDbToken(member.email, refreshToken)

        return JwtTokenDTO(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun logout(commonUserInfo: CommonUserInfo) {
        val member = memberService.getMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = "회원이 존재하지 않습니다.",
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )
        tokenManager.deleteCacheToken(member.email)
        tokenManager.revokDbToken(member.email)
    }

    private fun createAccessJwtToken(
        email: String,
        provider: LoginType,
        role: List<String>
    ): String {
        return jwtProvider.createAccessToken(
            email = email,
            provider = provider,
            roles = role
        )
    }

    private fun createRefreshJwtToken(
        email: String,
        provider: LoginType,
        role: List<String>
    ): String {
        return jwtProvider.createRefreshToken(
            email = email,
            provider = provider,
            roles = role
        )
    }
}