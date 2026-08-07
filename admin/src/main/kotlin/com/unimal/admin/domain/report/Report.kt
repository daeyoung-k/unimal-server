package com.unimal.admin.domain.report

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 신고 — 어드민 쪽 정의.
 *
 * 접수는 `board` 가 하고([com.unimal.board.domain.report.Report] 참고) 여기서는 검토·처리만 한다.
 * 그래서 접수 관련 필드는 전부 `val` 이고, 처리 관련 필드만 `var` 다.
 *
 * **[targetId] 가 무엇을 가리키는지는 [targetType] 에 따라 다르다.** 이게 이 클래스에서
 * 가장 헷갈리는 지점이라 여기 적어둔다.
 *
 * | targetType | targetId 가 가리키는 것 |
 * |---|---|
 * | `POST`  | `unimal_board.board.id` |
 * | `REPLY` | `unimal_board.board_reply.id` |
 * | `USER`  | **`unimal_board.board_member.id`** |
 *
 * `USER` 가 함정이다. `board_member` 는 Kafka 로 동기화될 때 id 를 지정하지 않고 저장돼
 * 자체 시퀀스를 쓰므로, **`unimal_user.member.id` 와 값이 다르다.** 이 값으로 회원
 * 관리 화면의 [com.unimal.admin.domain.appmember.AppMember] 를 찾으면 엉뚱한 사람이
 * 나온다. 반드시 `board_member` 를 거쳐 이메일을 얻은 뒤 조회해야 한다.
 */
@Entity
@Table(name = "report", schema = "unimal_board")
open class Report(
    @Column(name = "reporter_email")
    val reporterEmail: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    @Column(name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    val reason: ReportReason,

    @Column(length = 500)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: ReportStatus = ReportStatus.PENDING,

    @Column(name = "admin_memo", length = 500)
    var adminMemo: String? = null,

    /** 처리한 관리자의 로그인 ID. */
    @Column(name = "reviewed_by", length = 100)
    var reviewedBy: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var reviewedAt: LocalDateTime? = null,
) : BaseIdEntity() {

    /**
     * 신고를 처리한다.
     *
     * 상태만 바꾸고 **대상 제재(게시글 숨김·회원 차단)는 하지 않는다.** 제재는 회원
     * 관리 화면에서 사유를 적어 수동으로 한다 — 신고 처리와 제재를 한 버튼에 묶으면
     * 잘못 눌렀을 때 파급이 크고, 실제로 제재가 필요한 신고는 일부다.
     */
    fun review(status: ReportStatus, adminLoginId: String, memo: String?) {
        require(status != ReportStatus.PENDING) {
            "처리 상태는 PENDING 일 수 없습니다."
        }

        this.status = status
        this.reviewedBy = adminLoginId
        this.adminMemo = memo?.trim()?.takeIf { it.isNotEmpty() }
        this.reviewedAt = LocalDateTime.now()
    }

    /** 처리를 취소하고 미처리로 되돌린다. 잘못 눌렀을 때를 위한 것이다. */
    fun revertToPending() {
        this.status = ReportStatus.PENDING
        this.reviewedBy = null
        this.reviewedAt = null
    }
}
