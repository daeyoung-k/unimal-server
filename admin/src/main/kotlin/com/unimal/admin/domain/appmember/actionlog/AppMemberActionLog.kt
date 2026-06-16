package com.unimal.admin.domain.appmember.actionlog

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "admin_member_action_log")
open class AppMemberActionLog(
    @Column(nullable = false)
    val adminMemberId: Long = 0,

    @Column(nullable = false, length = 50)
    val adminLoginId: String,

    @Column(nullable = false)
    val targetMemberId: Long = 0,

    @Column(nullable = false, length = 50)
    val targetMemberEmail: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    val actionType: AppMemberActionType,

    @Column(nullable = false, length = 500)
    val reason: String,

    @Column(columnDefinition = "text")
    val beforeValue: String? = null,

    @Column(columnDefinition = "text")
    val afterValue: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : BaseIdEntity()
