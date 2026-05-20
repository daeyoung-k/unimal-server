package com.unimal.admin.domain.report

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) {
    private val report = QReport.report

    fun reportList() {}
}