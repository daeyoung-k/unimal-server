package com.unimal.admin.domain.appmember.actionlog

import org.springframework.data.jpa.repository.JpaRepository

interface AppMemberActionLogRepository : JpaRepository<AppMemberActionLog, Long> {
    fun findTop20ByTargetMemberIdOrderByCreatedAtDescIdDesc(targetMemberId: Long): List<AppMemberActionLog>
}
