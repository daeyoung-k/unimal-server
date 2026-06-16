package com.unimal.admin.domain.appmember

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "member", schema = "unimal_user")
open class AppMember(
    @Column(length = 20)
    val name: String? = null,

    val profileImage: String? = null,

    @Column(length = 100)
    val nickname: String? = null,

    @Column(length = 50, nullable = false)
    val email: String,

    @Column(length = 20)
    val tel: String? = null,

    @Column(length = 10)
    val provider: String? = null,

    @Column(length = 10)
    val gender: String? = null,

    @Column
    val introduction: String? = null,

    val birthday: LocalDateTime? = null,

    val createdAt: LocalDateTime? = null,

    val updatedAt: LocalDateTime? = null,

    val nicknameUpdatedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    val status: UserStatus = UserStatus.ACTIVE,

    val withdrawalAt: LocalDateTime? = null,
) : BaseIdEntity()
