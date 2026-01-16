package com.unimal.user.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberDeviceRepository: JpaRepository<MemberDevice, Long> {
    fun findByMember(member: Member): MemberDevice?
}