package com.unimal.admin.service.report

import com.unimal.admin.domain.report.target.ReportedBoard
import com.unimal.admin.domain.report.target.ReportedBoardFile
import com.unimal.admin.domain.report.target.ReportedBoardMember
import com.unimal.admin.domain.report.target.ReportedReply

/**
 * 신고 대상을 화면에 보여주기 위한 표현.
 *
 * 신고 한 건의 대상은 게시글·댓글·회원 중 하나인데 셋의 생김새가 달라 화면에서 분기가
 * 필요하다. 그 분기를 템플릿의 `th:if` 로 겹겹이 쌓지 않도록 여기서 한 번만 갈라 둔다.
 *
 * **[kind] 로 분기한다.** 템플릿에서 `instanceof T(...)` 를 쓰면 Kotlin 중첩 클래스가
 * JVM 에서 `ReportTargetView$Post` 로 잡혀 SpEL 이 타입을 찾지 못할 수 있고, 표현식도
 * 길어서 읽기 어렵다. 문자열 비교가 안전하고 눈에도 잘 들어온다.
 *
 * **대상을 못 찾는 경우가 정상적으로 존재한다.** 신고 접수 후 작성자가 글을 지우거나
 * 회원이 탈퇴하면 행이 사라진다. 그때는 [Missing] 을 준다 — 예외를 던지면 신고 한
 * 건 때문에 화면 전체가 죽는다.
 */
sealed interface ReportTargetView {

    /** 템플릿 분기용. `POST` / `REPLY` / `USER` / `MISSING`. */
    val kind: String

    data class Post(
        val board: ReportedBoard,
        val images: List<ReportedBoardFile>,
        /** 작성자. `board_member` 에서 찾지 못하면 null (탈퇴 등). */
        val author: ReportedBoardMember?,
    ) : ReportTargetView {
        override val kind: String get() = "POST"
    }

    data class Reply(
        val reply: ReportedReply,
        /** 댓글이 달린 글. 맥락 없이 댓글만 보면 판단할 수 없다. */
        val board: ReportedBoard?,
        val author: ReportedBoardMember?,
    ) : ReportTargetView {
        override val kind: String get() = "REPLY"
    }

    data class User(
        val member: ReportedBoardMember,
    ) : ReportTargetView {
        override val kind: String get() = "USER"
    }

    /** 신고 후 대상이 삭제되었거나 조회에 실패한 경우. */
    data class Missing(
        val note: String,
    ) : ReportTargetView {
        override val kind: String get() = "MISSING"
    }
}
