package com.unimal.user.service.login


import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.dto.kafka.user.UpdateUser
import com.unimal.common.enums.UserStatus
import com.unimal.user.controller.request.*
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.TokenManager
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.DuplicatedException
import com.unimal.webcommon.exception.TelNotFoundException
import com.unimal.webcommon.exception.WithdrawalException
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
    private val memberKafkaTopic: MemberKafkaTopic,

    private val memberRepository: MemberRepository,
) {

    @Transactional
    fun login(loginRequest: LoginRequest): JwtTokenDTO? {

        val provider: LoginType = loginRequest.provider
        val userInfo = getUserInfo(loginRequest)
        val member = getMember(loginRequest, userInfo)

        // 재가입
        if (member.status == UserStatus.RESIGNIN) {
            member.reSignIn(
                name = userInfo.name,
                nickname = userInfo.nickname,
                profileImage = userInfo.profileImage,
            )
            memberRepository.save(member)

            memberKafkaTopic.reSignInTopicIssue(
                UpdateUser(
                    email = member.email,
                    name = member.name,
                    nickname = member.nickname,
                    profileImage = member.profileImage
                )
            )
        }

        // 전화번호가 없음
        if (member.tel.isNullOrEmpty()) {
            throw TelNotFoundException(data = member.email)
        }

        // 탈퇴 상태
        if (member.status == UserStatus.WITHDRAWAL) {
            throw WithdrawalException()
        }

        val roles = member.roles.map { it.roleName.name }
        return tokenManager.createJwtToken(member.email, member.nickname ?: "", provider, roles)
    }

    @Transactional
    fun signup(signupRequest: SignupRequest) {
        memberRepository.findByEmail(signupRequest.email)?.let { throw DuplicatedException(ErrorCode.EMAIL_USED.message) }
        memberRepository.findByTel(signupRequest.tel)?.let { throw DuplicatedException(ErrorCode.TEL_USED.message) }

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
        val member = memberRepository.findByEmailAndProvider(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider).name
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
        memberRepository.findByTel(tel)?.let { throw DuplicatedException(ErrorCode.TEL_USED.message) }

        val member = memberRepository.findByEmail(email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        member.updateMember(tel = tel)
        val updateMember = memberRepository.save(member)

        val provider = LoginType.from(updateMember.provider)
        val roles = member.roles.map { it.roleName.name }
        return tokenManager.createJwtToken(updateMember.email, member.nickname ?: "", provider, roles)
    }

    @Transactional
    fun withdrawal(commonUserInfo: CommonUserInfo) {
        val member = memberRepository.findByEmailAndProvider(
            commonUserInfo.email,
            LoginType.from(commonUserInfo.provider).name
        ) ?: throw UserNotFoundException(
            message = ErrorCode.USER_NOT_FOUND.message,
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )

        tokenManager.deleteCacheToken(member.email)
        tokenManager.deleteDbToken(member.email)

        member.withdrawal()
        memberRepository.save(member)
        memberKafkaTopic.withdrawalTopicIssue(member.email)
    }

    private fun getUserInfo(
        loginRequest: LoginRequest
    ) = when (loginRequest) {
        is KakaoLoginRequest -> kakaoLoginObject.getUserInfo(loginRequest.token)
        is NaverLoginRequest -> naverLoginObject.getUserInfo(loginRequest)
        is GoogleLoginRequest -> googleLoginObject.getUserInfo(loginRequest)
        is ManualLoginRequest -> manualLoginObject.getUserInfo(loginRequest)
    }

    private fun getMember(
        loginRequest: LoginRequest,
        userInfo: UserInfo
    ) = when (loginRequest) {
        is KakaoLoginRequest -> kakaoLoginObject.getMember(userInfo)
        is NaverLoginRequest -> naverLoginObject.getMember(userInfo)
        is GoogleLoginRequest -> googleLoginObject.getMember(userInfo)
        is ManualLoginRequest -> manualLoginObject.getMember(userInfo)
    }
}