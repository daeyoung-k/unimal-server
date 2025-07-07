package com.unimal.user.service.login


import com.unimal.common.dto.CommonUserInfo
import com.unimal.user.controller.request.*
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.domain.member.Member
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.TokenManager
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.service.member.MemberObject
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
    private val memberObject: MemberObject,
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
            throw TelNotFoundException(data = member.email)
        }

        // 재가입
        if (member.withdrawalAt != null) {
            memberObject.reSignIn(member)
        }

        val roles = member.roles.map { it.roleName.name }
        return tokenManager.createJwtToken(member.email, provider, roles)
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

        if (signupRequest.password.lowercase() != signupRequest.checkPassword.lowercase()) {
            throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
        }

        if (!memberObject.passwordFormatCheck(signupRequest.password.lowercase())) {
            throw LoginException(ErrorCode.PASSWORD_FORMAT_INVALID.message)
        }

        manualLoginObject as ManualLoginObject
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
    fun telCheckUpdate(email: String, tel: String): JwtTokenDTO {
        val member = memberObject.getEmailMember(email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        member.updateMember(tel = tel)
        val updateMember = memberObject.update(member)

        val provider = LoginType.from(updateMember.provider)
        val roles = member.roles.map { it.roleName.name }
        return tokenManager.createJwtToken(updateMember.email, provider, roles)
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
}