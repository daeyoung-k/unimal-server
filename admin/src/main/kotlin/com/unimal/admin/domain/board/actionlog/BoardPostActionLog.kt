package com.unimal.admin.domain.board.actionlog

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 게시글에 대한 관리자 조치 이력 — 회원 관리의
 * [com.unimal.admin.domain.appmember.actionlog.AppMemberActionLog] 와 같은 패턴.
 *
 * 블락은 사용자 콘텐츠를 강제로 내리는 조치라 **누가·언제·왜** 를 남기지 않으면
 * CS 문의나 분쟁 때 근거가 없다. [beforeValue] 에 블락 직전 `show` 값을 남기는 것이
 * 특히 중요하다 — 해제할 때 원래 공개 상태로 되돌리는 유일한 근거다
 * (덮어쓴 뒤에는 board 테이블 어디에도 이전 값이 없다).
 */
@Entity
@Table(name = "admin_board_action_log")
open class BoardPostActionLog(
    @Column(nullable = false)
    val adminMemberId: Long = 0,

    @Column(nullable = false, length = 50)
    val adminLoginId: String,

    @Column(nullable = false)
    val boardId: Long = 0,

    @Column(nullable = false, length = 50)
    val boardEmail: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    val actionType: BoardPostActionType,

    @Column(nullable = false, length = 500)
    val reason: String,

    /** 조치 직전 `show` 값. 블락 해제 시 복구 기준. */
    @Column(length = 20)
    val beforeValue: String? = null,

    @Column(length = 20)
    val afterValue: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : BaseIdEntity()
