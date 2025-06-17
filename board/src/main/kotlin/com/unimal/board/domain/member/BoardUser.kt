package com.unimal.board.domain.member

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board_user")
open class BoardUser(
    val email: String,
    val nickname: String,
    val profileImageUrl: String,
    val withdrawalAt: LocalDateTime? = null,
) : BaseIdEntity()