package com.unimal.board.domain.report

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "report")
open class Report(
    @Column("reporter_email")
    val reporterEmail: String,

    @Enumerated(EnumType.STRING)
    @Column("target_type")
    val targetType: ReportTargetType,

    @Column("target_id")
    val targetId: Long,

    @Column(length = 50)
    val reason: String,

    @Column(length = 500)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val status: ReportStatus = ReportStatus.PENDING,

    @Column("admin_memo", length = 500)
    val adminMemo: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val reviewedAt: LocalDateTime? = null
) : BaseIdEntity() {
    companion object {
        fun create(
            reporterEmail: String,
            targetType: ReportTargetType,
            targetId: Long,
            reason: String,
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