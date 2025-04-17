package com.unimal.user.domain.member

import com.unimal.common.domain.BaseIdEntity
import com.unimal.user.domain.role.MemberRole
import com.unimal.user.domain.role.Role
import com.unimal.user.domain.role.enums.MemberRoleCode
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
open class Member(

    @Column(length = 20)
    val name: String? = null,

    @Column(length = 30)
    val nickname: String? = null,

    @Column(length = 50, unique = true, nullable = false)
    val email: String,

    @Column(length = 20)
    val tel: String? = null,

    @Column(length = 10)
    val provider: String? = null,

    @Column(length = 200)
    val password: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null,

): BaseIdEntity() {
    @OneToMany(mappedBy = "memberId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val roles: MutableList<MemberRole> = mutableListOf()

    fun addRole(role: MemberRole) {
        roles.add(role)
    }

    fun getMemberRole(role: Role): MemberRole {
        return MemberRole(
            memberId = this,
            roleName = role
        ).also {
            this.addRole(it)
        }
    }

}