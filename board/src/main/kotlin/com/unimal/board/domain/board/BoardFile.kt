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

    // 마커용 400px JPEG 썸네일 파생. 백필 전 기존 파일은 null (앱은 fileUrl 폴백)
    val thumbKey: String? = null,
    val thumbUrl: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,

    ) : BaseIdEntity()