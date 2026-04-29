package com.unimal.admin.service.report

import com.unimal.admin.domain.report.ReportRepository
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository
) {

    fun getReport() {
        val reports = reportRepository.findAll()

        reports.forEach {
            print(it)
        }

    }
}