package com.unimal.user.service.member.dto

import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberDevice

data class MemberDeviceInfo(
    val fcmToken: String? = null,
    val model: String? = null,
    val systemName: String? = null,
    val systemVersion: String? = null
) {
    fun toEntity(
        member: Member
    ): MemberDevice {
        return MemberDevice(
            member = member,
            fcmToken = fcmToken,
            model = model,
            systemName = systemName,
            systemVersion = systemVersion
        )
    }
}
