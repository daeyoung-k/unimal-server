package com.unimal.admin.domain.report.target

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.hibernate.annotations.Immutable

/**
 * 신고된 댓글 — **읽기 전용**.
 *
 * [boardId] 를 함께 들고 오는 이유는, 댓글만 보면 맥락을 알 수 없기 때문이다.
 * "그럴 수도 있죠" 같은 댓글이 어떤 글에 달렸느냐에 따라 판단이 달라진다.
 */
@Entity
@Immutable
@Table(name = "board_reply", schema = "unimal_board")
open class ReportedReply(
    @Column(name = "board_id")
    val boardId: Long = 0,

    @Column(name = "email", length = 50)
    val email: String = "",

    @Column(columnDefinition = "text")
    val comment: String = "",

    val del: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
) : BaseIdEntity()
