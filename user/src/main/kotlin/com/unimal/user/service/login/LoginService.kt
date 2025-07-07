package com.unimal.user.service.login


import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.controller.request.*
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.domain.member.Member
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.JwtProvider
import com.unimal.user.service.token.TokenManager
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.service.member.MemberObject
import com.unimal.user.utils.RedisCacheManager
import com.unimal.webcommon.exception.AuthCodeException
import com.unimal.webcommon.exception.DuplicatedException
import com.unimal.webcommon.exception.TelNotFoundException
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
    private val memberObject: MemberObject,
    private val redisCacheManager: RedisCacheManager
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

        // 전화번호가 없음
        if (member.tel.isNullOrEmpty()) {
            throw TelNotFoundException()
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
            email = member.email,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun signup(signupRequest: SignupRequest) {
        val checkEmail = memberObject.getEmailMember(signupRequest.email)
        if (checkEmail != null) {
            throw DuplicatedException(ErrorCode.EMAIL_USED.message)
        }

        val checkTel = memberObject.getTelMember(signupRequest.tel)
        if (checkTel != null) {
            throw DuplicatedException(ErrorCode.TEL_USED.message)
        }

        manualLoginObject as ManualLoginObject
        if (!manualLoginObject.passwordCheck(signupRequest.password.lowercase())) {
            throw LoginException(ErrorCode.PASSWORD_FORMAT_INVALID.message)
        }

        if (!manualLoginObject.emailTelSuccessCheck(signupRequest.email, signupRequest.tel)) {
            throw LoginException(ErrorCode.AUTHENTICATION_NOT_COMPLETED.message)
        }

        val userInfo = signupRequest.toUserInfo()
        memberObject.signIn(userInfo)
    }

    @Transactional
    fun logout(commonUserInfo: CommonUserInfo) {
        val member = memberObject.getEmailProviderMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = ErrorCode.USER_NOT_FOUND.message,
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )
        tokenManager.deleteCacheToken(member.email)
        tokenManager.revokDbToken(member.email)
    }

    @Transactional
    fun telCheckUpdate(telCheckUpdateRequest: TelCheckUpdateRequest) {
        val key = "${telCheckUpdateRequest.email}:${telCheckUpdateRequest.tel}:auth-code"
        val check = redisCacheManager.getCache(key)
        if (check.isNullOrEmpty() || check != "SUCCESS") {
            throw AuthCodeException(ErrorCode.AUTHENTICATION_NOT_COMPLETED.message)
        }

        val member = memberObject.getEmailMember(telCheckUpdateRequest.email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        member.updateMember(tel = telCheckUpdateRequest.tel)
        memberObject.update(member)
    }

    @Transactional
    fun withdrawal(commonUserInfo: CommonUserInfo) {
        val member = memberObject.getEmailProviderMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = ErrorCode.USER_NOT_FOUND.message,
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