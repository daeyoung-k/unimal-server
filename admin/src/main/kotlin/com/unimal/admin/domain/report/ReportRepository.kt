package com.unimal.admin.domain.report

import com.unimal.common.enums.report.ReportStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ReportRepository :
    JpaRepository<Report, Long>,
    JpaSpecificationExecutor<Report> {

    fun countByStatus(status: ReportStatus): Long
}
