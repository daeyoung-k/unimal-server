package com.unimal.board.domain.report

import com.unimal.common.enums.report.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository: JpaRepository<Report, Long> {

    fun existsByReporterEmailAndTargetTypeAndTargetId(
        reporterEmail: String,
        targetType: ReportTargetType,
        targetId: Long,
    ): Boolean
}
