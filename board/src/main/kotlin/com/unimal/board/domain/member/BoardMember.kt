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
    val name: String? = null,
    @Column(length = 30)
    var nickname: String? = null,

    var profileImage: String? = null,

    var withdrawalAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    var status: UserStatus = UserStatus.ACTIVE,

) : BaseIdEntity() {
    fun nicknameUpdate(nickname: String) {
        this.nickname = nickname
    }
    fun profileImageUpdate(profileImage: String?) {
        this.profileImage = profileImage
    }
    fun withdrawal(
        withdrawalAt: LocalDateTime,
        status: UserStatus
    ) {
        this.withdrawalAt = withdrawalAt
        this.status = status
    }
    fun reSignIn() {
        this.withdrawalAt = null
        this.status = UserStatus.ACTIVE
    }
}