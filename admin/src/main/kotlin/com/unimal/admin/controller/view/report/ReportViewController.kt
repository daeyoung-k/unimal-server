package com.unimal.admin.controller.view.report

import com.unimal.admin.service.report.ReportSearchCondition
import com.unimal.admin.service.report.ReportService
import com.unimal.admin.service.report.ReportSort
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 신고 관리 화면.
 *
 * 목록에서는 무엇이 신고됐는지 훑고, 상세에서 실제 내용을 보고 처리한다. 신고는
 * **내용을 보지 않고는 판단할 수 없어서** 다른 화면과 달리 상세가 반드시 필요하다.
 */
@Controller
@RequestMapping("/reports")
class ReportViewController(
    private val reportService: ReportService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String? = null,
        @RequestParam(required = false) targetType: String? = null,
        @RequestParam(required = false) reason: String? = null,
        @RequestParam(defaultValue = "oldest") sort: String = "oldest",
        model: Model
    ): String {
        val condition = ReportSearchCondition(
            status = ReportSearchCondition.normalizeStatus(status),
            targetType = ReportSearchCondition.normalizeTargetType(targetType),
            reason = ReportSearchCondition.normalizeReason(reason),
            sort = ReportSort.from(sort)
        )

        val reportsPage = reportService.getReports(page, size, condition)

        model.addAttribute("reportsPage", reportsPage)
        model.addAttribute("page", reportsPage.number)
        model.addAttribute("size", reportsPage.size)
        model.addAttribute("filter", condition)
        model.addAttribute("pendingCount", reportService.countPending())
        model.addAttribute("statuses", ReportStatus.entries)
        model.addAttribute("targetTypes", ReportTargetType.entries)
        model.addAttribute("reasons", ReportReason.entries)
        model.addAttribute("sortOptions", ReportSort.entries)

        return "report/list"
    }

    @GetMapping("/{reportId}")
    fun detail(
        @PathVariable reportId: Long,
        model: Model
    ): String {
        val report = reportService.getReport(reportId)

        model.addAttribute("report", report)
        model.addAttribute("target", reportService.resolveTarget(report))

        return "report/detail"
    }

    @PostMapping("/{reportId}/review")
    fun review(
        @PathVariable reportId: Long,
        @RequestParam status: ReportStatus,
        @RequestParam(required = false) memo: String?,
        authentication: Authentication
    ): String {
        reportService.review(
            reportId = reportId,
            status = status,
            adminLoginId = authentication.name,
            memo = memo
        )

        return "redirect:/reports/$reportId"
    }

    /** 잘못 처리했을 때 되돌린다. */
    @PostMapping("/{reportId}/revert")
    fun revert(@PathVariable reportId: Long): String {
        reportService.revertToPending(reportId)

        return "redirect:/reports/$reportId"
    }
}
