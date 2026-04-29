package com.unimal.board.service.report

import com.unimal.board.controller.report.dto.ReportCreateRequest
import com.unimal.board.domain.report.Report
import com.unimal.board.domain.report.ReportRepository
import com.unimal.common.dto.CommonUserInfo
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository
) {

    @Transactional
    fun saveReport(
        userInfo: CommonUserInfo,
        reportCreateRequest: ReportCreateRequest
    ) {
        reportRepository.save(
            Report.create(
                reporterEmail = userInfo.email,
                targetType = reportCreateRequest.targetType,
                targetId = reportCreateRequest.targetId,
                reason = reportCreateRequest.reason,
                description = reportCreateRequest.description
            )
        )
    }
}