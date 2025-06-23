package com.unimal.user.service


import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.controller.request.GoogleLoginRequest
import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.LoginRequest
import com.unimal.user.controller.request.ManualLoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.authentication.login.LoginInterface
import com.unimal.user.service.authentication.token.JwtProvider
import com.unimal.user.service.authentication.token.TokenManager
import com.unimal.user.service.authentication.token.dto.JwtTokenDTO
import com.unimal.user.service.member.MemberObject
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLoginObject") private val kakaoLoginObject: LoginInterface,
    @Qualifier("NaverLoginObject") private val naverLoginObject: LoginInterface,
    @Qualifier("GoogleLoginObject") private val googleLoginObject: LoginInterface,
    @Qualifier("ManualLoginObject") private val manualLoginObject: LoginInterface,
    private val tokenManager: TokenManager,
    private val jwtProvider: JwtProvider,
    private val memberObject: MemberObject
) {

    @Transactional
    fun login(loginRequest: LoginRequest): JwtTokenDTO? {

        val provider: LoginType = loginRequest.provider
        val member: Member = when (loginRequest) {
            is KakaoLoginRequest -> {
                val userInfo = kakaoLoginObject.getUserInfo(loginRequest.token)
                kakaoLoginObject.getMember(userInfo)
            }
            is NaverLoginRequest -> {
                val userInfo = naverLoginObject.getUserInfo(loginRequest)
                naverLoginObject.getMember(userInfo)
            }
            is GoogleLoginRequest -> {
                val userInfo = googleLoginObject.getUserInfo(loginRequest)
                googleLoginObject.getMember(userInfo)
            }
            is ManualLoginRequest -> {
                val userInfo = manualLoginObject.getUserInfo(loginRequest)
                manualLoginObject.getMember(userInfo)
            }
            else -> {
                throw LoginException(ErrorCode.LOGIN_NOT_SUPPORTED.message)
            }
        }

        // 재가입
        if (member.withdrawalAt != null) {
            memberObject.reSignIn(member)
        }

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
        val member = memberObject.getMember(
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

    @Transactional
    fun withdrawal(commonUserInfo: CommonUserInfo) {
        val member = memberObject.getMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = "회원이 존재하지 않습니다.",
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )

        tokenManager.deleteCacheToken(member.email)
        tokenManager.deleteDbToken(member.email)
        memberObject.withdrawal(member)
        memberObject.withdrawalTopicIssue(member)
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