package com.unimal.board.domain.board

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "board_file")
open class BoardFile(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", referencedColumnName = "id")
    val board: Board,

    val main: Boolean = false,

    val fileName: String? = null,
    val fileKey: String? = null,
    val fileUrl: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,

    ) : BaseIdEntity()