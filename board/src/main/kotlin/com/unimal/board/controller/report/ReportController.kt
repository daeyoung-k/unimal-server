package com.unimal.board.controller.report

import com.unimal.board.controller.report.dto.ReportCreateRequest
import com.unimal.board.service.report.ReportService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/report")
class ReportController(
    private val reportService: ReportService
) {

    @PostMapping
    fun saveReport(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @RequestBody @Valid reportCreateRequest: ReportCreateRequest
    ): CommonResponse {
        reportService.saveReport(userInfo, reportCreateRequest)
        return CommonResponse(code = 201)
    }
}