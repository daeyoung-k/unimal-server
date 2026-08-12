package com.unimal.admin.domain.board.actionlog

import org.springframework.data.jpa.repository.JpaRepository

interface BoardPostActionLogRepository : JpaRepository<BoardPostActionLog, Long> {

    /** 상세 화면 이력 패널. 최근 것부터. */
    fun findTop20ByBoardIdOrderByCreatedAtDescIdDesc(boardId: Long): List<BoardPostActionLog>

    /** 블락 해제 시 복구할 이전 `show` 값을 찾는다 — 가장 최근 블락 로그 기준. */
    fun findTopByBoardIdAndActionTypeOrderByIdDesc(
        boardId: Long,
        actionType: BoardPostActionType,
    ): BoardPostActionLog?
}
