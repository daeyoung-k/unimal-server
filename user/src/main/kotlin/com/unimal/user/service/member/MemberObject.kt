package com.unimal.user.service.member

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.member.dto.MemberInfo
import org.springframework.stereotype.Component

@Component
class MemberObject(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val roleRepository: RoleRepository,
) {
    fun getMember(email: String, provider: LoginType) = memberRepository.findByEmailAndProvider(email, provider.name)

    fun signIn(userInfo: UserInfo): Member {
        val member = memberRepository.save(userInfo.toEntity())
        val role = roleRepository.findByName(MemberRoleCode.USER.name)
            ?: throw LoginException(ErrorCode.ROLE_NOT_FOUND.message)
        memberRoleRepository.save(member.getMemberRole(role))
        return member
    }

    fun getMemberInfo(email: String, provider: LoginType): MemberInfo {
        return getMember(email, provider)?.let { member ->
            MemberInfo(
                email = member.email,
                nickName = member.nickname,
                provider = provider.name,
            )
        } ?: throw LoginException(ErrorCode.USER_NOT_FOUND.message)
    }
}