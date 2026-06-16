package com.unimal.admin.domain.adminmember

import com.unimal.admin.domain.adminmember.enums.AdminMemberStatus
import com.unimal.admin.domain.adminmember.enums.AdminRole
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "admin_member")
open class AdminMember(
    @Column(name = "login_id")
    val loginId: String,
    val password: String,
    val name: String,
    val email: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: AdminMemberStatus = AdminMemberStatus.ACTIVE,

    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
) : BaseIdEntity() {

    @OneToMany(mappedBy = "adminMember", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val roles: MutableList<AdminMemberRole> = mutableListOf()

    fun addRole(role: AdminRole): AdminMemberRole {
        return AdminMemberRole(
            adminMember = this,
            role = role
        ).also {
            roles.add(it)
        }
    }
}
