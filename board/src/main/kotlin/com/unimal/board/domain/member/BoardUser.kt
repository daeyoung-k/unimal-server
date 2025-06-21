package com.unimal.board.domain.member

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board_user")
open class BoardUser(
    @Column(length = 50, unique = true, nullable = false)
    val email: String,

    @Column(length = 20)
    val name: String? = null,
    @Column(length = 30)
    val nickname: String? = null,

    val profileImageUrl: String? = null,

    var withdrawalAt: LocalDateTime? = null,
) : BaseIdEntity()