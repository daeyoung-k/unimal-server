package com.unimal.board.controller.report.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportTargetType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReportCreateRequest(
    @field:NotNull
    @JsonProperty(value = "target_type")
    val targetType: ReportTargetType,

    @field:NotBlank
    @JsonProperty(value = "target_id")
    val targetId: String,

    @field:NotNull
    val reason: ReportReason,

    @field:Size(max = 500)
    val description: String? = null,
)
