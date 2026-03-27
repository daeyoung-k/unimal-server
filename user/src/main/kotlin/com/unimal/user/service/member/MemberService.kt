package com.unimal.user.service.member

import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.dto.kafka.UpdateUser
import com.unimal.common.enums.Gender
import com.unimal.common.extension.toPatternLocalDate
import com.unimal.common.extension.toPatternLocalDateTime
import com.unimal.common.extension.toPatternString
import com.unimal.user.controller.request.ChangePasswordInterface
import com.unimal.user.controller.request.EmailRequest
import com.unimal.user.controller.request.InfoUpdateRequest
import com.unimal.user.controller.request.TelRequest
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.kafka.topics.MemberKafkaTopic
import com.unimal.user.service.file.FileService
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.member.dto.FindEmailInfo
import com.unimal.user.service.member.dto.MemberInfo
import com.unimal.webcommon.exception.*
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MemberService(
    private val memberObject: MemberObject,
    private val fileService: FileService,
    private val memberKafkaTopic: MemberKafkaTopic,

    private val memberRepository: MemberRepository,

    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,
) {

    fun getMemberInfo(
        commonUserInfo: CommonUserInfo
    ): MemberInfo {
        val provider = LoginType.from(commonUserInfo.provider)
        return memberRepository.findByEmailAndProvider(commonUserInfo.email, provider.name)?.let { member ->
            MemberInfo(
                email = member.email,
                provider = provider.name,
                nickname = member.nickname,
                name = member.name,
                tel = member.tel,
                birthday = member.birthday?.toPatternString("yyyy-MM-dd"),
                gender = Gender.from(member.gender)?.name,
                introduction = member.introduction,
                profileImage = member.profileImage
            )
        } ?: throw LoginException(ErrorCode.USER_NOT_FOUND.message)
    }

    @Transactional
    fun updateMemberInfo(
        commonUserInfo: CommonUserInfo,
        infoUpdateRequest: InfoUpdateRequest
    ) {
        val member = memberObject.getEmailProviderMember(commonUserInfo.email, LoginType.from(commonUserInfo.provider)) ?: throw UserNotFoundException("회원이 존재하지 않습니다.")

        if (!infoUpdateRequest.name.isNullOrBlank() && infoUpdateRequest.name != member.name) {
            member.updateMember(name = infoUpdateRequest.name)
        }

        if (!infoUpdateRequest.nickname.isNullOrBlank() && infoUpdateRequest.nickname != member.nickname) {
            member.updateMember(nickname = infoUpdateRequest.nickname)
        }

        if (!infoUpdateRequest.tel.isNullOrBlank() && infoUpdateRequest.tel != member.tel) {
            member.updateMember(tel = infoUpdateRequest.tel)
        }

        if (!infoUpdateRequest.introduction.isNullOrBlank() && infoUpdateRequest.introduction != member.introduction) {
            member.updateMember(introduction = infoUpdateRequest.introduction)
        }

        if (!infoUpdateRequest.birthday.isNullOrBlank()) {
            val birthday = infoUpdateRequest.birthday.toPatternLocalDate("yyyy-MM-dd").atStartOfDay()
            if (birthday != member.birthday) {
                member.updateMember(birthday = birthday)
            }
        }

        if (!infoUpdateRequest.gender.isNullOrBlank()) {
            val gender = Gender.from(infoUpdateRequest.gender) ?: throw CustomException("잘못된 성별 정보입니다")
            if (gender.name != member.gender) {
                member.updateMember(gender = gender.name)
            }
        }
    }

    fun getDuplicatedEmailCheck(emailRequest: EmailRequest) {
        memberRepository.findByEmail(emailRequest.email)?.let { throw DuplicatedException(ErrorCode.EMAIL_USED.message) }
    }

    fun getDuplicatedTelCheck(telRequest: TelRequest) {
        memberRepository.findByTel(telRequest.tel)?.let { throw DuplicatedException(ErrorCode.TEL_USED.message) }
    }

    fun findEmailByTel(
        tel: String
    ): FindEmailInfo {
        val member = memberRepository.findByTel(tel)
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
        val member = memberRepository.findByTel(tel) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        if (member.email != email) {
            throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

    @Transactional
    fun changePassword(changePassword: ChangePasswordInterface) {
        memberRepository.findByEmail(changePassword.email!!)?.let { member ->
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
            member.passwordUpdate(password)

        } ?: run { throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message) }
    }

    fun findNicknameDuplicate(nickname: String) {
        if (memberObject.nicknameSlangCheck(nickname)) throw InvalidException("비속어가 포함된 닉네임입니다.")
        memberRepository.findByNickname(nickname)?.let {
            throw DuplicatedException(ErrorCode.NICKNAME_USED.message)
        }
    }

    @Transactional
    fun uploadProfileImage(
        email: String,
        image: MultipartFile
    ) {
        val member = memberRepository.findByEmail(email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)

        val uploadFileInfo = fileService.uploadFileHttp(image, "profile")
        val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key

        if (member.profileImage?.contains(fileBaseUrl) == true) {
            val key = listOf(member.profileImage!!.replace("$fileBaseUrl/", ""))
            fileService.deleteFileHttp(key)
        }

        member.updateProfileImage(fileUrl)

        memberKafkaTopic.userUpdateTopicIssue(
            UpdateUser(
                email = member.email,
                profileImage = member.profileImage
            )
        )
    }
}