package com.unimal.user.service.authentication.login

import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.LoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import com.unimal.user.service.authentication.login.kakao.KakaoLogin
import com.unimal.user.service.authentication.login.naver.NaverLoginService
import com.unimal.user.service.authentication.token.TokenManager
import com.unimal.user.service.authentication.token.dto.JwtTokenDTO
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLogin") private val kakaoLoginService: Login,
    @Qualifier("NaverLogin") private val naverLoginService: Login,
    private val tokenManager: TokenManager
) {

    @Transactional
    fun login(loginRequest: LoginRequest): JwtTokenDTO? {

        when (loginRequest) {
            is KakaoLoginRequest -> {
                kakaoLoginService as KakaoLogin

                val userInfo = kakaoLoginService.getInfo(loginRequest.token)
                val member = kakaoLoginService.getMember(userInfo.email) ?: kakaoLoginService.signIn(userInfo)
                val roles = member.roles.map { it.roleName.name }

                val accessToken = kakaoLoginService.createAccessJwtToken(member.email, roles)
                tokenManager.saveCacheToken(member.email, accessToken)

                val refreshToken = kakaoLoginService.createRefreshJwtToken(member.email, roles)
                tokenManager.upsertDbToken(member.email, refreshToken)

                return JwtTokenDTO(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            }
            is NaverLoginRequest -> {
                naverLoginService.getInfo(loginRequest)
                return JwtTokenDTO(
                    accessToken = "accessToken",
                    refreshToken = "refreshToken"
                )
            }
            else -> {
                throw LoginException(ErrorCode.LOGIN_NOT_SUPPORTED.message)
            }
        }

    }
}