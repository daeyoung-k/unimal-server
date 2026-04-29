package com.unimal.admin.domain.report

import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository: JpaRepository<Report, Long> {
}