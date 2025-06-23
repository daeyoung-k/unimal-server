package com.unimal.board.domain.board

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board")
open class Board(

    val title: String,
    val content: String,
    val siDo: String,
    val guGun: String,
    val dong: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
    val email: String

    ) : BaseIdEntity()