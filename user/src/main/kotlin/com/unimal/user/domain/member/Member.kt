package com.unimal.user.domain.member

import com.unimal.common.domain.BaseIdEntity
import com.unimal.user.domain.role.MemberRole
import com.unimal.user.domain.role.Role
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
open class Member(

    @Column(length = 20)
    var name: String? = null,

    val profileImage: String? = null,

    @Column(length = 100, unique = true)
    var nickname: String? = null,

    @Column(length = 50, unique = true, nullable = false)
    val email: String,

    @Column(length = 20)
    var tel: String? = null,

    @Column(length = 10)
    val provider: String? = null,

    @Column(length = 200)
    var password: String? = null,

    @Column(length = 10)
    var gender: String? = null,

    @Column
    var introduction: String? = null,

    var birthday: LocalDateTime? = null,

    @CreatedDate
    val createdAt: LocalDateTime? = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null,
    var nicknameUpdatedAt: LocalDateTime? = null,
    var withdrawalAt: LocalDateTime? = null,

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

    fun updateMember(
        name: String? = null,
        nickname: String? = null,
        tel: String? = null,
        introduction: String? = null,
        birthday: LocalDateTime? = null,
        gender: String? = null,
    ) {
        if (name != null) this.name = name
        if (nickname != null) this.nickname = nickname
        if (tel != null) this.tel = tel
        if (introduction != null) this.introduction = introduction
        if (birthday != null) this.birthday = birthday
        if (gender != null) this.gender = gender
        this.updatedAt = LocalDateTime.now()
    }

}