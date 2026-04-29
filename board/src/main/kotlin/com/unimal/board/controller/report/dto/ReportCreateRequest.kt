package com.unimal.board.controller.report.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unimal.common.enums.report.ReportTargetType

data class ReportCreateRequest(
    @JsonProperty(value = "target_type")
    val targetType: ReportTargetType,
    @JsonProperty(value = "target_id")
    val targetId: Long,
    val reason: String,
    val description: String? = null
)
