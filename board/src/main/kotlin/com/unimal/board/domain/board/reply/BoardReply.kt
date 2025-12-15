package com.unimal.board.domain.board.reply

import com.unimal.board.domain.board.Board
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "board_reply")
open class BoardReply(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", referencedColumnName = "id")
    val board: Board,

    @Column(name = "email", length = 50)
    val email: String,

    @Column(columnDefinition = "text", nullable = false)
    var content: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
) : BaseIdEntity()