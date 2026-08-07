package com.unimal.admin.service.report

import com.unimal.admin.domain.report.Report
import com.unimal.admin.domain.report.ReportRepository
import com.unimal.admin.domain.report.target.ReportedBoardFileRepository
import com.unimal.admin.domain.report.target.ReportedBoardMemberRepository
import com.unimal.admin.domain.report.target.ReportedBoardRepository
import com.unimal.admin.domain.report.target.ReportedReplyRepository
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 신고 검토·처리.
 *
 * 접수는 앱에서 `board` 가 받는다. 여기서는 쌓인 신고를 보고 처리 상태만 정한다.
 */
@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val reportedBoardRepository: ReportedBoardRepository,
    private val reportedBoardFileRepository: ReportedBoardFileRepository,
    private val reportedReplyRepository: ReportedReplyRepository,
    private val reportedBoardMemberRepository: ReportedBoardMemberRepository,
) {

    @Transactional(readOnly = true)
    fun getReports(
        page: Int,
        size: Int,
        condition: ReportSearchCondition = ReportSearchCondition()
    ): Page<Report> {
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            condition.sort.toSort()
        )

        return reportRepository.findAll(condition.toSpecification(), pageable)
    }

    @Transactional(readOnly = true)
    fun getReport(reportId: Long): Report {
        return reportRepository.findById(reportId)
            .orElseThrow { NoSuchElementException("Report not found: $reportId") }
    }

    @Transactional(readOnly = true)
    fun countPending(): Long = reportRepository.countByStatus(ReportStatus.PENDING)

    /**
     * 신고 대상을 화면에 보여줄 형태로 해석한다.
     *
     * **회원 신고의 `targetId` 는 `board_member.id` 다.** `unimal_user.member.id` 가
     * 아니므로 곧장 회원 관리 엔티티를 조회하면 안 된다 — 자세한 내용은
     * [Report] KDoc 참고. 여기서 이메일을 얻어 화면에서 회원 상세로 이어준다.
     */
    @Transactional(readOnly = true)
    fun resolveTarget(report: Report): ReportTargetView {
        return when (report.targetType) {
            ReportTargetType.POST -> {
                val board = reportedBoardRepository.findById(report.targetId).orElse(null)
                    ?: return ReportTargetView.Missing("삭제되었거나 찾을 수 없는 게시글입니다.")

                ReportTargetView.Post(
                    board = board,
                    images = reportedBoardFileRepository
                        .findByBoardIdOrderByMainDescIdAsc(board.id!!),
                    author = findMemberByEmail(board.email),
                )
            }

            ReportTargetType.REPLY -> {
                val reply = reportedReplyRepository.findById(report.targetId).orElse(null)
                    ?: return ReportTargetView.Missing("삭제되었거나 찾을 수 없는 댓글입니다.")

                ReportTargetView.Reply(
                    reply = reply,
                    board = reportedBoardRepository.findById(reply.boardId).orElse(null),
                    author = findMemberByEmail(reply.email),
                )
            }

            ReportTargetType.USER -> {
                val member = reportedBoardMemberRepository.findById(report.targetId).orElse(null)
                    ?: return ReportTargetView.Missing("탈퇴했거나 찾을 수 없는 회원입니다.")

                ReportTargetView.User(member)
            }
        }
    }

    @Transactional
    fun review(
        reportId: Long,
        status: ReportStatus,
        adminLoginId: String,
        memo: String?,
    ): Report {
        val report = getReport(reportId)
        report.review(status = status, adminLoginId = adminLoginId, memo = memo)

        return report
    }

    @Transactional
    fun revertToPending(reportId: Long): Report {
        val report = getReport(reportId)
        report.revertToPending()

        return report
    }

    /**
     * `board_member` 를 이메일로 찾는다.
     *
     * id 로 찾지 않는 이유는 게시글·댓글이 작성자를 이메일로 들고 있기 때문이다
     * (`board.email`, `board_reply.email`). 어차피 이메일이 두 스키마를 잇는
     * 유일하게 믿을 수 있는 키다.
     */
    private fun findMemberByEmail(email: String) =
        reportedBoardMemberRepository.findByEmail(email)

    private fun ReportSearchCondition.toSpecification(): Specification<Report> =
        Specification { root, _, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            status?.let {
                predicates.add(criteriaBuilder.equal(root.get<ReportStatus>("status"), it))
            }

            targetType?.let {
                predicates.add(criteriaBuilder.equal(root.get<ReportTargetType>("targetType"), it))
            }

            reason?.let {
                predicates.add(criteriaBuilder.equal(root.get<ReportReason>("reason"), it))
            }

            if (predicates.isEmpty()) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
}
