package com.unimal.admin.domain.report

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
@Table(name = "report", schema = "unimal_board")
open class Report(
    @Column(name = "reporter_email")
    val reporterEmail: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    @Column(name = "target_id")
    val targetId: Long,

    @Column(length = 50)
    val reason: String,

    @Column(length = 500)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    val status: ReportStatus = ReportStatus.PENDING,

    @Column(name = "admin_memo", length = 500)
    val adminMemo: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val reviewedAt: LocalDateTime? = null
) : BaseIdEntity()