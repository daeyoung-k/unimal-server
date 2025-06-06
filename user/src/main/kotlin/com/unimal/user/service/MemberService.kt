package com.unimal.user.service

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.Gender
import com.unimal.common.extension.toPatternLocalDateTime
import com.unimal.user.controller.request.InfoUpdateRequest
import com.unimal.user.exception.CustomException
import com.unimal.user.exception.UserNotFoundException
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.member.MemberObject
import com.unimal.user.service.member.dto.MemberInfo
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberObject: MemberObject
) {

    fun getMemberInfo(
        commonUserInfo: CommonUserInfo
    ): MemberInfo {
        return memberObject.getMemberInfo(commonUserInfo.email, LoginType.from(commonUserInfo.provider))
    }

    @Transactional
    fun updateMemberInfo(
        commonUserInfo: CommonUserInfo,
        infoUpdateRequest: InfoUpdateRequest
    ) {
        val member = memberObject.getMember(commonUserInfo.email, LoginType.from(commonUserInfo.provider)) ?: throw UserNotFoundException("회원이 존재하지 않습니다.")

        if (!infoUpdateRequest.name.isNullOrBlank() && infoUpdateRequest.name != member.name) {
            member.updateMember(name = infoUpdateRequest.name)
            memberObject.update(member)
        }

        if (!infoUpdateRequest.nickname.isNullOrBlank() && infoUpdateRequest.nickname != member.nickname) {
            member.updateMember(nickname = infoUpdateRequest.nickname)
            memberObject.update(member)
        }

        if (!infoUpdateRequest.tel.isNullOrBlank() && infoUpdateRequest.tel != member.tel) {
            member.updateMember(tel = infoUpdateRequest.tel)
            memberObject.update(member)
        }

        if (!infoUpdateRequest.introduction.isNullOrBlank() && infoUpdateRequest.introduction != member.introduction) {
            member.updateMember(introduction = infoUpdateRequest.introduction)
            memberObject.update(member)
        }

        if (!infoUpdateRequest.birthday.isNullOrBlank()) {
            val birthday = infoUpdateRequest.birthday.toPatternLocalDateTime("yyyy-MM-dd HH:mm")
            if (birthday != member.birthday) {
                member.updateMember(birthday = birthday)
                memberObject.update(member)
            }
        }

        if (!infoUpdateRequest.gender.isNullOrBlank()) {
            val gender = Gender.from(infoUpdateRequest.gender) ?: throw CustomException("잘못된 성별 정보입니다")
            if (gender.name != member.gender) {
                member.updateMember(gender = infoUpdateRequest.nickname)
                memberObject.update(member)
            }
        }
    }
}