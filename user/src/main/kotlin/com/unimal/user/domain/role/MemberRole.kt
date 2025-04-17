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
    var memberId: Member,

    @ManyToOne
    @JoinColumn(name = "name", referencedColumnName = "name")
    val roleName: Role

) : BaseIdEntity()