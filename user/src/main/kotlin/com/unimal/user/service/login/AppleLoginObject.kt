package com.unimal.user.service.login

import com.unimal.user.controller.request.AppleLoginRequest
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.service.login.apple.AppleIdentityTokenVerifier
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import org.springframework.stereotype.Component

/**
 * Sign in with Apple 로그인 처리.
 *
 * 다른 소셜과 다른 점 두 가지:
 * 1. 이메일/식별자를 클라이언트가 보낸 값이 아니라 identityToken 검증 결과에서 꺼낸다.
 * 2. 회원 조회 시 이메일보다 providerId(sub)를 우선한다.
 *    애플은 "이메일 가리기"를 켜고 끄면 relay 이메일이 바뀔 수 있어 이메일이 영구 식별자가 아니다.
 */
@Component("AppleLoginObject")
class AppleLoginObject(
    private val memberObject: MemberObject,
    private val memberRepository: MemberRepository,
    private val appleIdentityTokenVerifier: AppleIdentityTokenVerifier,
) : LoginInterface {

    override fun provider(): LoginType = LoginType.APPLE

    override fun <T> getUserInfo(info: T): UserInfo {
        if (info !is AppleLoginRequest) {
            throw LoginException(ErrorCode.LOGIN_NOT_SUPPORTED.message)
        }

        val payload = appleIdentityTokenVerifier.verify(info.identityToken)

        // 애플은 재로그인 시 email claim을 생략하는 경우가 있어, 없으면 기존 회원 정보에서 찾는다.
        val email = payload.email
            ?: memberRepository.findByProviderAndProviderId(provider().name, payload.sub)?.email
            ?: throw LoginException(ErrorCode.APPLE_EMAIL_NOT_FOUND.message)

        return UserInfo(
            provider = provider().name,
            email = email,
            providerId = payload.sub,
            name = info.name,
            // 애플은 닉네임 개념이 없다. 최초 1회 내려오는 이름을 닉네임 기본값으로 쓴다.
            nickname = info.nickname ?: info.name,
        )
    }

    override fun getMember(userInfo: UserInfo): Member {
        val providerId = userInfo.providerId

        // 1순위: Apple sub
        if (!providerId.isNullOrBlank()) {
            memberRepository.findByProviderAndProviderId(provider().name, providerId)?.let { return it }
        }

        // 2순위: 이메일 (providerId 도입 이전 가입자 호환)
        // 기존 소셜 로그인과 동일하게 이메일이 같으면 같은 계정으로 본다.
        memberRepository.findByEmail(userInfo.email)?.let { member ->
            // 단, 애플 계정에만 sub를 채운다. 다른 provider 계정에 애플 sub를 덮어쓰면 안 된다.
            val isAppleMember = member.provider == provider().name
            if (isAppleMember && member.providerId.isNullOrBlank() && !providerId.isNullOrBlank()) {
                member.updateProviderId(providerId)
                memberRepository.save(member)
            }
            return member
        }

        return memberObject.signIn(userInfo)
    }
}
