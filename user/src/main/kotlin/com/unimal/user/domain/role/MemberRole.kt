package com.unimal.user.domain.role

import com.unimal.common.domain.BaseIdEntity
import com.unimal.user.domain.member.Member
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "member_role")
open class MemberRole(

    @ManyToOne
    @JoinColumn(name = "member_id")
    val memberId: Member,

    @ManyToOne
    @JoinColumn(name = "role_id")
    val roleId: Role

) : BaseIdEntity()