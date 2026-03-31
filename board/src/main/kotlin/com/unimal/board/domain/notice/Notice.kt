package com.unimal.board.domain.notice

import com.unimal.board.controller.notice.dto.NoticeResponse
import com.unimal.board.enums.NoticeType
import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "notice")
open class Notice(
    @Enumerated(EnumType.STRING)
    val type: NoticeType,
    val title: String,

    @Column(columnDefinition = "TEXT")
    val content: String,

    val show: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
) : BaseIdEntity() {
    fun toResponse(): NoticeResponse = NoticeResponse(
        id = id.toString(),
        type = type.name,
        title = title,
        content = content,
        createdAt = createdAt.toLocalDate().toString()
    )
}