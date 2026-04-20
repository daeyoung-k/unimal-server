package com.unimal.board.domain.member

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board_member")
open class BoardMember(
    @Column(length = 50, unique = true, nullable = false)
    val email: String,

    @Column(length = 20)
    var name: String? = null,
    @Column(length = 30)
    var nickname: String? = null,

    var profileImage: String? = null,

    var withdrawalAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    var status: UserStatus = UserStatus.ACTIVE,

    var fcmToken: String? = null

) : BaseIdEntity() {
    fun nameUpdate(name: String) {
        this.name = name
    }

    fun nicknameUpdate(nickname: String) {
        this.nickname = nickname
    }

    fun profileImageUpdate(profileImage: String?) {
        this.profileImage = profileImage
    }

    fun withdrawal() {
        this.status = UserStatus.WITHDRAWAL
        this.name = null
        this.nickname =null
        this.profileImage = null
        this.fcmToken = null
        this.withdrawalAt = LocalDateTime.now()
    }

    fun reSignIn(
        name: String?,
        nickname: String?,
        profileImage: String?
    ) {
        this.status = UserStatus.ACTIVE
        this.name = name
        this.nickname = nickname
        this.profileImage = profileImage
    }

    fun fcmTokenUpdate(fcmToken: String) {
        this.fcmToken = fcmToken
    }
}