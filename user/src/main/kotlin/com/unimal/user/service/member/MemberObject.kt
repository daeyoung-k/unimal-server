package com.unimal.user.service.member

import com.unimal.common.dto.kafka.SignInUser
import com.unimal.common.enums.Gender
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
import com.unimal.user.service.member.dto.MemberInfo
import com.unimal.user.utils.RedisCacheManager
import com.unimal.webcommon.exception.InvalidException
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

    fun getEmailMember(email: String): Member? = memberRepository.findByEmail(email)

    fun getTelMember(tel: String): Member? = memberRepository.findByTel(tel)

    fun getNicknameMember(nickname: String): Member? = memberRepository.findByNickname(nickname)

    fun passwordCheck(
        password: String,
        encodePassword: String
    ): Boolean = passwordEncoder.matches(password, encodePassword)

    fun signIn(userInfo: UserInfo): Member {

        // 닉네임 중복체크 & null 일때 unimal uuid 닉네임 생성 입력

        val member = memberRepository.save(userInfo.toEntity())
        val role = roleRepository.findByName(MemberRoleCode.USER.name)
            ?: throw LoginException(ErrorCode.ROLE_NOT_FOUND.message)
        memberRoleRepository.save(member.getMemberRole(role))
        // 회원가입 토픽 발행
        signInTopicIssue(member)
        return member
    }

    fun getMemberInfo(email: String, provider: LoginType): MemberInfo {
        return getEmailProviderMember(email, provider)?.let { member ->
            MemberInfo(
                email = member.email,
                provider = provider.name,
                nickname = member.nickname,
                name = member.name,
                tel = member.tel,
                birthday = member.birthday?.toPatternString("yyyy-MM-dd HH:mm"),
                gender = Gender.from(member.gender)?.name,
                introduction = member.introduction,
            )
        } ?: throw LoginException(ErrorCode.USER_NOT_FOUND.message)
    }

    fun update(member: Member) = memberRepository.save(member)

    fun withdrawal(member: Member) {
        member.withdrawalAt = LocalDateTime.now()
        memberRepository.save(member)
    }

    fun reSignIn(member: Member) {
        member.withdrawalAt = null
        memberRepository.save(member)
        memberKafkaTopic.reSignInTopicIssue(member.email)
    }

    fun signInTopicIssue(member: Member) {
        memberKafkaTopic.signInTopicIssue(
            SignInUser(
                email = member.email,
                name = member.name,
                nickname = member.nickname,
                profileImage = member.profileImage,
                withdrawalAt = member.withdrawalAt
            )
        )
    }

    fun withdrawalTopicIssue(member: Member) {
        memberKafkaTopic.withdrawalTopicIssue(member.email)
    }

    fun passwordFormatCheck(password: String): Boolean {
        // 비밀번호는 8자 ~ 20자 사이, 영문, 숫자, 특수문자를 포함.
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,20}$")
        return regex.matches(password)
    }

    fun passwordEncode(password: String): String {
        return passwordEncoder.encode(password.lowercase())
    }

    fun nicknameSlangCheck(nickname: String): Boolean {
        val cacheProfanity = redisCacheManager.getCacheSet(SlangType.PROFANITY.name)
        val hasSlang = cacheProfanity.any { slang -> nickname.contains(slang) }
        if (hasSlang) {
            throw InvalidException("비속어가 포함된 닉네임입니다.")
        }
        return true
    }

}