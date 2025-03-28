package com.unimal.user.service.login

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import org.springframework.stereotype.Component

@Component
class LoginObjects(
    private val memberRepository: MemberRepository
) {
    fun memberCheck(
        email: String,
        provider: String
    ): Member? = memberRepository.findByEmailAndProvider(email, provider)
}