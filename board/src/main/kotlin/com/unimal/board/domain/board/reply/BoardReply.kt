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

    val replyId: Long? = null,

    @Column(name = "email", length = 50)
    val email: String,

    @Column(columnDefinition = "text", nullable = false)
    var comment: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,

    // nullable 이었으나 non-null 로 변경 (2026-07-29).
    // nullable 이면 조회 조건을 coalesce(del, false) = false 로 써야 하는데,
    // 그러면 부분 인덱스 idx_board_reply_board(board_id) WHERE del = false 의
    // predicate 함의를 플래너가 증명할 수 없어 인덱스를 버리고 전체 스캔한다.
    // DB 에 del IS NULL 인 행은 0건이었고, NOT NULL 제약을 걸었다.
    @Column(nullable = false)
    var del: Boolean = false,
) : BaseIdEntity()