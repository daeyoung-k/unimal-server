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
import com.unimal.user.service.login.apple.AppleAuthClient
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.DuplicatedException
import com.unimal.webcommon.exception.TelNotFoundException
import com.unimal.webcommon.exception.WithdrawalException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class LoginService(
    @Qualifier("KakaoLoginObject") private val kakaoLoginObject: LoginInterface,
    @Qualifier("NaverLoginObject") private val naverLoginObject: LoginInterface,
    @Qualifier("GoogleLoginObject") private val googleLoginObject: LoginInterface,
    @Qualifier("AppleLoginObject") private val appleLoginObject: LoginInterface,
    @Qualifier("ManualLoginObject") private val manualLoginObject: LoginInterface,
    private val appleAuthClient: AppleAuthClient,
    private val tokenManager: TokenManager,
    private val memberObject: MemberObject,
    private val memberKafkaTopic: MemberKafkaTopic,

    private val memberRepository: MemberRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun login(loginRequest: LoginRequest): JwtTokenDTO? {

        val userInfo = getUserInfo(loginRequest)
        val member = getMember(loginRequest, userInfo)

        // 탈퇴 시 Apple revoke에 쓸 refresh_token 확보 (실패해도 로그인은 진행)
        if (loginRequest is AppleLoginRequest) {
            updateAppleRefreshToken(member, loginRequest.authorizationCode)
        }

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
        val provider = LoginType.from(member.provider)
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
    fun signupV2(signupRequest: SignupV2Request): Nothing {
        memberRepository.findByEmail(signupRequest.email)
            ?.let { throw DuplicatedException(ErrorCode.EMAIL_USED.message) }

        if (signupRequest.password.lowercase() != signupRequest.checkPassword.lowercase()) {
            throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
        }

        if (!memberObject.passwordFormatCheck(signupRequest.password.lowercase())) {
            throw LoginException(ErrorCode.PASSWORD_FORMAT_INVALID.message)
        }

        manualLoginObject as ManualLoginObject
        if (!manualLoginObject.emailSuccessCheck(signupRequest.email)) {
            throw LoginException(ErrorCode.AUTHENTICATION_NOT_COMPLETED.message)
        }

        val member = memberObject.signIn(signupRequest.toUserInfo())

        // TelNotFoundException은 RuntimeException이 아닌 CustomException(checked)이므로
        // @Transactional이 롤백하지 않고 member 저장이 먼저 커밋됨 — 소셜 로그인 tel-missing 플로우와 동일
        throw TelNotFoundException(data = member.email)
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
        val member = memberObject.getEmailProviderMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = ErrorCode.USER_NOT_FOUND.message,
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )

        // 애플 연결 해제 — 계정 삭제 시 토큰 revoke는 애플 심사 요구사항이다 (TN3194 / 가이드 5.1.1(v))
        revokeAppleToken(member)

        tokenManager.deleteCacheToken(member.email)
        tokenManager.deleteDbToken(member.email)

        member.withdrawal()
        memberRepository.save(member)
        memberKafkaTopic.withdrawalTopicIssue(member.email)
    }

    private fun updateAppleRefreshToken(member: Member, authorizationCode: String?) {
        if (authorizationCode.isNullOrBlank()) return

        // 로그인할 때마다 최신 refresh_token으로 갱신한다.
        // 사용자가 iOS 설정에서 앱 연결을 끊었다가 다시 로그인하면 기존 토큰은 죽고
        // 새 연결이 생기므로, 저장해둔 값을 그대로 믿으면 탈퇴 시 revoke가 조용히 실패한다.
        // 소셜 로그인 자체가 자주 일어나지 않는 구조라(세션은 토큰 재발급으로 유지)
        // 트랜잭션 안에서 외부 호출 한 번 도는 비용보다 정확성이 중요하다.

        runCatching { appleAuthClient.exchangeAuthorizationCode(authorizationCode) }
            .onSuccess { response ->
                val refreshToken = response?.refreshToken
                if (refreshToken.isNullOrBlank()) {
                    logger.warn { "애플 refresh_token 미수신 - email=${member.email}, error=${response?.error}" }
                    return@onSuccess
                }
                member.updateAppleRefreshToken(refreshToken)
                memberRepository.save(member)
            }
            .onFailure { logger.warn(it) { "애플 authorization_code 교환 실패 - email=${member.email}" } }
    }

    private fun revokeAppleToken(member: Member) {
        if (LoginType.from(member.provider) != LoginType.APPLE) return

        val refreshToken = member.appleRefreshToken
        if (refreshToken.isNullOrBlank()) {
            logger.warn { "애플 탈퇴 - refresh_token이 없어 revoke를 건너뜁니다. email=${member.email}" }
            return
        }

        runCatching { appleAuthClient.revoke(refreshToken) }
            .onFailure { logger.error(it) { "애플 revoke 실패 - email=${member.email}" } }
    }

    private fun getUserInfo(
        loginRequest: LoginRequest
    ) = when (loginRequest) {
        is KakaoLoginRequest -> kakaoLoginObject.getUserInfo(loginRequest.token)
        is NaverLoginRequest -> naverLoginObject.getUserInfo(loginRequest)
        is GoogleLoginRequest -> googleLoginObject.getUserInfo(loginRequest)
        is AppleLoginRequest -> appleLoginObject.getUserInfo(loginRequest)
        is ManualLoginRequest -> manualLoginObject.getUserInfo(loginRequest)
    }

    private fun getMember(
        loginRequest: LoginRequest,
        userInfo: UserInfo
    ) = when (loginRequest) {
        is KakaoLoginRequest -> kakaoLoginObject.getMember(userInfo)
        is NaverLoginRequest -> naverLoginObject.getMember(userInfo)
        is GoogleLoginRequest -> googleLoginObject.getMember(userInfo)
        is AppleLoginRequest -> appleLoginObject.getMember(userInfo)
        is ManualLoginRequest -> manualLoginObject.getMember(userInfo)
    }
}
