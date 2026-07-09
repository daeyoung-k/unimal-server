package com.unimal.board.service.report

import com.unimal.board.controller.report.dto.ReportCreateRequest
import com.unimal.board.domain.board.BoardRepository
import com.unimal.board.domain.board.reply.BoardReplyRepository
import com.unimal.board.domain.member.BoardMemberRepository
import com.unimal.board.domain.report.Report
import com.unimal.board.domain.report.ReportRepository
import com.unimal.board.enums.PostShow
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.UserStatus
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportTargetType
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.ReportException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val boardRepository: BoardRepository,
    private val boardReplyRepository: BoardReplyRepository,
    private val boardMemberRepository: BoardMemberRepository,
    private val hashidsUtil: HashidsUtil,
) {

    @Transactional
    fun saveReport(
        userInfo: CommonUserInfo,
        reportCreateRequest: ReportCreateRequest
    ) {
        // 1. 사유 검증 - ETC는 상세 내용 필수
        if (reportCreateRequest.reason == ReportReason.ETC && reportCreateRequest.description.isNullOrBlank()) {
            throw ReportException(ErrorCode.REPORT_DESCRIPTION_REQUIRED.message)
        }

        // 2~3. 대상 해석 + 존재 검증 + 자기 신고 방지 → 정규화 id
        val targetId = resolveAndValidateTarget(userInfo.email, reportCreateRequest)

        // 4. 중복 신고 방지 (서비스 레벨)
        if (reportRepository.existsByReporterEmailAndTargetTypeAndTargetId(
                userInfo.email, reportCreateRequest.targetType, targetId
            )
        ) {
            throw ReportException(ErrorCode.REPORT_ALREADY_EXISTS.message)
        }

        // 5. 저장 (DB unique 제약은 동시 요청 race의 최종 방어선)
        try {
            reportRepository.save(
                Report.create(
                    reporterEmail = userInfo.email,
                    targetType = reportCreateRequest.targetType,
                    targetId = targetId,
                    reason = reportCreateRequest.reason,
                    description = reportCreateRequest.description
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw ReportException(ErrorCode.REPORT_ALREADY_EXISTS.message)
        }
    }

    /**
     * 신고 대상을 해석/검증하고 내부 정규화 id를 반환한다.
     * - 사용자가 정상적으로 볼 수 있는 대상(삭제/탈퇴/비공개/차단 아님)만 허용.
     * - 자기 자신 신고 차단.
     */
    private fun resolveAndValidateTarget(reporter: String, req: ReportCreateRequest): Long {
        return when (req.targetType) {
            ReportTargetType.POST -> {
                val board = boardRepository.findBoardById(hashidsUtil.decode(req.targetId))
                    ?.takeIf { !it.del && it.show == PostShow.PUBLIC }
                    ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                if (board.email.email == reporter) {
                    throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                }
                board.id!!
            }

            ReportTargetType.REPLY -> {
                val reply = boardReplyRepository.findById(hashidsUtil.decode(req.targetId))
                    .orElse(null)
                    ?.takeIf { it.del != true && !it.board.del && it.board.show == PostShow.PUBLIC }
                    ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                if (reply.email == reporter) {
                    throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                }
                reply.id!!
            }

            ReportTargetType.USER -> {
                if (req.targetId == reporter) {
                    throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                }
                val member = boardMemberRepository.findByEmail(req.targetId)
                    ?.takeIf { it.status == UserStatus.ACTIVE }
                    ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                member.id!!
            }
        }
    }
}
