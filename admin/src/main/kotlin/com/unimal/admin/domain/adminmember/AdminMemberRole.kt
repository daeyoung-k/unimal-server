package com.unimal.admin.domain.adminmember

import com.unimal.admin.domain.adminmember.enums.AdminRole
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "admin_member_role",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_admin_member_role",
            columnNames = ["admin_member_id", "role"]
        )
    ]
)
open class AdminMemberRole(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_member_id", nullable = false)
    val adminMember: AdminMember,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    val role: AdminRole,
) : BaseIdEntity()
