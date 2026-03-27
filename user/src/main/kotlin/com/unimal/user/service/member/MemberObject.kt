package com.unimal.user.service.member

import com.unimal.common.dto.kafka.SignInUser
import com.unimal.common.extension.toPatternString
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.domain.slang.SlangType
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.utils.RedisCacheManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MemberObject(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val roleRepository: RoleRepository,
    private val memberKafkaTopic: MemberKafkaTopic,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val redisCacheManager: RedisCacheManager
) {
    fun getEmailProviderMember(email: String, provider: LoginType) = memberRepository.findByEmailAndProvider(email, provider.name)

    fun signIn(userInfo: UserInfo): Member {

        if (!userInfo.nickname.isNullOrBlank()) {
            userInfo.nickname = signInNicknameCheck(userInfo.nickname!!)
        }

        val member = memberRepository.save(userInfo.toEntity())
        val role = roleRepository.findByName(MemberRoleCode.USER.name)
            ?: throw LoginException(ErrorCode.ROLE_NOT_FOUND.message)
        memberRoleRepository.save(member.getMemberRole(role))
        // 회원가입 토픽 발행
        memberKafkaTopic.signInTopicIssue(
            SignInUser(
                email = member.email,
                name = member.name,
                nickname = member.nickname,
                profileImage = member.profileImage,
                withdrawalAt = member.withdrawalAt,
                status = member.status
            )
        )
        return member
    }

    fun passwordCheck(
        password: String,
        encodePassword: String
    ): Boolean = passwordEncoder.matches(password, encodePassword)

    fun passwordFormatCheck(password: String): Boolean {
        // 비밀번호는 8자 ~ 20자 사이, 영문, 숫자, 특수문자를 포함.
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,20}$")
        return regex.matches(password)
    }

    fun passwordEncode(password: String): String {
        return passwordEncoder.encode(password.lowercase())
    }

    fun signInNicknameCheck(nickname: String): String {
        val timestamp = LocalDateTime.now().toPatternString("yyyyMMddHHmmss")
        val unimalNickname = "UNIMAL_$timestamp"
        // 비속어 체크
        if (nicknameSlangCheck(nickname)) return unimalNickname
        // 중복 닉네임 체크
        memberRepository.findByNickname(nickname)?.let { return unimalNickname }
        return nickname
    }

    fun nicknameSlangCheck(nickname: String): Boolean {
        val cacheProfanity = redisCacheManager.getStringCacheSet(SlangType.PROFANITY.name)
        val hasSlang = cacheProfanity.any { slang -> nickname.contains(slang) }
        return hasSlang
    }

}