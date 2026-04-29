package com.unimal.admin.controller.report

import com.unimal.admin.service.report.ReportService
import com.unimal.common.dto.CommonResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/report")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping
    fun getReport(): CommonResponse {
        reportService.getReport()
        return CommonResponse()
    }
}