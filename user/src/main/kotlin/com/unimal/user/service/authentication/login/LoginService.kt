package com.unimal.user.service.authentication.login

import com.unimal.user.controller.request.KakaoLoginRequest
import com.unimal.user.controller.request.LoginRequest
import com.unimal.user.controller.request.NaverLoginRequest
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import com.unimal.user.service.authentication.login.kakao.KakaoLogin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLogin") private val kakaoLoginService: Login
) {

    /**
     * Processes a user login request based on the type of social login provider.
     *
     * Supports Kakao login by retrieving user information and signing in if the user does not exist.
     * Throws a LoginException if the login type is not supported.
     *
     * @param loginRequest The login request containing provider-specific credentials.
     * @throws LoginException If the login type is not supported.
     */
    fun login(loginRequest: LoginRequest) {

        when (loginRequest) {
            is KakaoLoginRequest -> {
                kakaoLoginService as KakaoLogin
                val userInfo = kakaoLoginService.getInfo(loginRequest.token)
                val member = kakaoLoginService.getMember(userInfo.email) ?: kakaoLoginService.signIn(userInfo)
                println(member)
            }
            is NaverLoginRequest -> {

            }
            else -> {
                throw LoginException(ErrorCode.LOGIN_NOT_SUPPORTED.message)
            }
        }

    }
}