package com.unimal.board.domain.report

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_report_reporter_target",
            columnNames = ["reporter_email", "target_type", "target_id"]
        )
    ]
)
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
    val status: ReportStatus = ReportStatus.PENDING,

    @Column(name = "admin_memo", length = 500)
    val adminMemo: String? = null,

    @Column(name = "reviewed_by", length = 100)
    val reviewedBy: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val reviewedAt: LocalDateTime? = null
) : BaseIdEntity() {
    companion object {
        fun create(
            reporterEmail: String,
            targetType: ReportTargetType,
            targetId: Long,
            reason: ReportReason,
            description: String? = null
        ): Report {
            return Report(
                reporterEmail = reporterEmail,
                targetType = targetType,
                targetId = targetId,
                reason = reason,
                description = description
            )
        }
    }

}
