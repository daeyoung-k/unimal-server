package com.unimal.admin.controller.view.appmember

import com.unimal.admin.service.appmember.AppMemberService
import com.unimal.common.enums.UserStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
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
        model: Model
    ): String {
        val membersPage = appMemberService.getMembers(page, size)

        model.addAttribute("membersPage", membersPage)
        model.addAttribute("page", membersPage.number)
        model.addAttribute("size", membersPage.size)
        model.addAttribute("statuses", UserStatus.entries)

        return "appmember/list"
    }
}
