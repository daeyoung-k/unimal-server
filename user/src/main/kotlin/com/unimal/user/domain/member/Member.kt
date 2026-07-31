package com.unimal.user.domain.member

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.UserStatus
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

    var profileImage: String? = null,

    @Column(length = 100, unique = true)
    var nickname: String? = null,

    @Column(length = 50, unique = true, nullable = false)
    val email: String,

    @Column(length = 20)
    var tel: String? = null,

    @Column(length = 10)
    val provider: String? = null,

    /**
     * 소셜 제공자가 발급한 고유 사용자 식별자.
     * Apple의 경우 identityToken의 sub claim.
     * 애플은 사용자가 "이메일 가리기"를 켜고 끄면 relay 이메일이 바뀔 수 있어
     * 이메일만으로는 동일인 식별이 보장되지 않는다.
     */
    @Column(length = 100)
    var providerId: String? = null,

    /**
     * Apple 로그인 전용. 탈퇴 시 Apple /auth/revoke 호출에 사용한다.
     * (App Store 심사 가이드 5.1.1(v) / TN3194 대응)
     */
    @Column(length = 500)
    var appleRefreshToken: String? = null,

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

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    var status: UserStatus = UserStatus.ACTIVE,

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
        if (nickname != null) {
            this.nickname = nickname
            this.nicknameUpdatedAt = LocalDateTime.now()
        }
        if (tel != null) this.tel = tel
        if (introduction != null) this.introduction = introduction
        if (birthday != null) this.birthday = birthday
        if (gender != null) this.gender = gender
        this.updatedAt = LocalDateTime.now()
    }

    fun updateProviderId(providerId: String) {
        this.providerId = providerId
        this.updatedAt = LocalDateTime.now()
    }

    fun updateAppleRefreshToken(appleRefreshToken: String?) {
        this.appleRefreshToken = appleRefreshToken
        this.updatedAt = LocalDateTime.now()
    }

    fun updateProfileImage(
        profileImage: String
    ) {
        this.profileImage = profileImage
        this.updatedAt = LocalDateTime.now()
    }

    fun withdrawal() {
        this.status = UserStatus.WITHDRAWAL
        this.withdrawalAt = LocalDateTime.now()
        this.tel = null
        this.profileImage = null
        this.name = null
        this.nickname = null
        this.introduction = null
        this.birthday = null
        this.gender = null
        this.appleRefreshToken = null
        this.updatedAt = LocalDateTime.now()
    }

    fun reSignIn(
        name: String?,
        nickname: String?,
        profileImage: String?
    ) {
        this.status = UserStatus.ACTIVE
        this.nickname = nickname
        this.name = name
        this.profileImage = profileImage
        this.updatedAt = LocalDateTime.now()
    }

    fun passwordUpdate(password: String) {
        this.password = password
        this.updatedAt = LocalDateTime.now()
    }

}