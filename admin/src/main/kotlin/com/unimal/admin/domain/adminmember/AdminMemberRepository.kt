package com.unimal.admin.domain.adminmember

import org.springframework.data.jpa.repository.JpaRepository

interface AdminMemberRepository : JpaRepository<AdminMember, Long> {
    fun findByLoginId(loginId: String): AdminMember?
}
