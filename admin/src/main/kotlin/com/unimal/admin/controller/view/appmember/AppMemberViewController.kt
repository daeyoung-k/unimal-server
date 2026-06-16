package com.unimal.admin.controller.view.appmember

import com.unimal.admin.service.appmember.AppMemberSearchCondition
import com.unimal.admin.service.appmember.AppMemberService
import com.unimal.admin.service.appmember.AppMemberSort
import com.unimal.common.enums.UserStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/members")
class AppMemberViewController(
    private val appMemberService: AppMemberService
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String? = null,
        @RequestParam(required = false) provider: String? = null,
        @RequestParam(required = false) keyword: String? = null,
        @RequestParam(defaultValue = "latest") sort: String = "latest",
        model: Model
    ): String {
        val condition = AppMemberSearchCondition(
            status = AppMemberSearchCondition.normalizeStatus(status),
            provider = provider,
            keyword = keyword,
            sort = AppMemberSort.from(sort)
        ).normalized()
        val membersPage = appMemberService.getMembers(page, size, condition)
        val providerCounts = appMemberService.getProviderCounts(condition)

        model.addAttribute("membersPage", membersPage)
        model.addAttribute("providerCounts", providerCounts)
        model.addAttribute("page", membersPage.number)
        model.addAttribute("size", membersPage.size)
        model.addAttribute("filter", condition)
        model.addAttribute("statuses", UserStatus.entries)
        model.addAttribute("providerOptions", AppMemberSearchCondition.providerOptions)
        model.addAttribute("sortOptions", AppMemberSort.entries)

        return "appmember/list"
    }

    @GetMapping("/{memberId}")
    fun detail(
        @PathVariable memberId: Long,
        model: Model
    ): String {
        model.addAttribute("member", appMemberService.getMember(memberId))
        model.addAttribute("actionLogs", appMemberService.getActionLogs(memberId))

        return "appmember/detail"
    }

    @PostMapping("/{memberId}/actions/reset-profile-image")
    fun resetProfileImage(
        @PathVariable memberId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        appMemberService.resetProfileImage(
            memberId = memberId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/members/$memberId"
    }

    @PostMapping("/{memberId}/actions/hide-introduction")
    fun hideIntroduction(
        @PathVariable memberId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        appMemberService.hideIntroduction(
            memberId = memberId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/members/$memberId"
    }

    @PostMapping("/{memberId}/actions/block")
    fun blockMember(
        @PathVariable memberId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        appMemberService.blockMember(
            memberId = memberId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/members/$memberId"
    }

    @PostMapping("/{memberId}/actions/unblock")
    fun unblockMember(
        @PathVariable memberId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        appMemberService.unblockMember(
            memberId = memberId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/members/$memberId"
    }
}
