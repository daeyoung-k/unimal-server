package com.unimal.user.service.member

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberDeviceRepository
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.service.member.dto.MemberDeviceInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberDeviceService(
    private val memberRepository: MemberRepository,
    private val memberDeviceRepository: MemberDeviceRepository,
) {

    @Transactional
    fun saveOrUpdate(
        email: String,
        memberDeviceInfo: MemberDeviceInfo,
    ) {
        memberRepository.findByEmail(email)?.let { member ->
            memberDeviceRepository.findByMember(member)?.let {
                it.fcmTokenUpdate(memberDeviceInfo.fcmToken)
                it.modelUpdate(memberDeviceInfo.model)
                it.systemNameUpdate(memberDeviceInfo.systemName)
                it.systemVersionUpdate(memberDeviceInfo.systemVersion)
                memberDeviceRepository.save(it)
            }
            ?: run {
                memberDeviceRepository.save(memberDeviceInfo.toEntity(member))
            }
        }
    }
}