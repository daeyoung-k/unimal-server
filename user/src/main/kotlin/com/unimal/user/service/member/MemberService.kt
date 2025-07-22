package com.unimal.user.service.member

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.Gender
import com.unimal.common.extension.toPatternLocalDateTime
import com.unimal.user.controller.request.ChangePasswordInterface
import com.unimal.user.controller.request.EmailRequest
import com.unimal.user.controller.request.InfoUpdateRequest
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.dto.FindEmailInfo
import com.unimal.user.service.member.dto.MemberInfo
import com.unimal.webcommon.exception.*
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
        val member = memberObject.getEmailProviderMember(commonUserInfo.email, LoginType.from(commonUserInfo.provider)) ?: throw UserNotFoundException("회원이 존재하지 않습니다.")

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

    fun getDuplicatedEmailCheck(emailRequest: EmailRequest) {
        val checkEmail = memberObject.getEmailMember(emailRequest.email)
        if (checkEmail != null) {
            throw DuplicatedException(ErrorCode.EMAIL_USED.message)
        }
    }

    fun findEmailByTel(
        tel: String
    ): FindEmailInfo {
        val member = memberObject.getTelMember(tel)

        val email = member?.email

        return FindEmailInfo(
            email = email,
            loginType = LoginType.from(member?.provider),
            message = if (email == null) "해당 전화번호로 회원을 찾을 수 없습니다." else "아이디 찾기 성공"
        )
    }

    fun checkEmailByTel(
        email: String,
        tel: String
    ) {
        val member = memberObject.getTelMember(tel) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        if (member.email != email) {
            throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

    @Transactional
    fun changePassword(changePassword: ChangePasswordInterface) {
        val member = memberObject.getEmailMember(changePassword.email!!) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)

        if (member.password != null && memberObject.passwordCheck(changePassword.newPassword.lowercase(), member.password!!)) {
            throw LoginException(ErrorCode.EXISTING_PASSWORD_NOT_CHANGE.message)
        }

        if (changePassword.oldPassword.lowercase() != changePassword.newPassword.lowercase()) {
            throw LoginException(ErrorCode.PASSWORD_NOT_MATCH.message)
        }

        if (!memberObject.passwordFormatCheck(changePassword.newPassword.lowercase())) {
            throw LoginException(ErrorCode.PASSWORD_FORMAT_INVALID.message)
        }

        val password = memberObject.passwordEncode(changePassword.newPassword)
        member.password = password
        memberObject.update(member)
    }
}