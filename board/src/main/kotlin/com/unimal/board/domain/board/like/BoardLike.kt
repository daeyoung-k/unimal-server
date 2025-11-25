package com.unimal.board.domain.board.like

import com.unimal.board.domain.board.Board
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "board_like")
open class BoardLike(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", referencedColumnName = "id")
    val board: Board,

    @Column(name = "email", length = 50)
    val email: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,

    ) : BaseIdEntity()