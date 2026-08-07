package com.unimal.admin.controller.view.notice

import com.unimal.admin.service.notice.NoticeSearchCondition
import com.unimal.admin.service.notice.NoticeService
import com.unimal.admin.service.notice.NoticeSort
import com.unimal.common.enums.notice.NoticeType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 공지사항 관리 화면.
 *
 * 목록·작성·수정만 있고 상세 조회 화면은 없다. 공지는 본문이 짧고 어드민이 볼 일은
 * "고치려고" 여는 경우가 대부분이라, 상세를 따로 두면 클릭만 한 번 더 늘어난다.
 *
 * 쓰기 동작은 전부 POST → redirect (PRG) 다. 새로고침으로 같은 공지가 두 번 올라가는
 * 사고를 막는다.
 */
@Controller
@RequestMapping("/notices")
class NoticeViewController(
    private val noticeService: NoticeService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) type: String? = null,
        @RequestParam(required = false) show: String? = null,
        @RequestParam(required = false) keyword: String? = null,
        @RequestParam(defaultValue = "latest") sort: String = "latest",
        model: Model
    ): String {
        val condition = NoticeSearchCondition(
            type = NoticeSearchCondition.normalizeType(type),
            show = NoticeSearchCondition.normalizeShow(show),
            keyword = keyword,
            sort = NoticeSort.from(sort)
        ).normalized()

        val noticesPage = noticeService.getNotices(page, size, condition)

        model.addAttribute("noticesPage", noticesPage)
        model.addAttribute("page", noticesPage.number)
        model.addAttribute("size", noticesPage.size)
        model.addAttribute("filter", condition)
        model.addAttribute("hiddenCount", noticeService.countHidden())
        model.addAttribute("types", NoticeType.entries)
        model.addAttribute("sortOptions", NoticeSort.entries)

        return "notice/list"
    }

    @GetMapping("/new")
    fun createForm(model: Model): String {
        model.addAttribute("types", NoticeType.entries)
        model.addAttribute("notice", null)

        return "notice/form"
    }

    @PostMapping
    fun create(
        @RequestParam type: NoticeType,
        @RequestParam title: String,
        @RequestParam content: String,
    ): String {
        noticeService.create(type = type, title = title, content = content)

        return "redirect:/notices"
    }

    @GetMapping("/{noticeId}/edit")
    fun editForm(
        @PathVariable noticeId: Long,
        model: Model
    ): String {
        model.addAttribute("types", NoticeType.entries)
        model.addAttribute("notice", noticeService.getNotice(noticeId))

        return "notice/form"
    }

    @PostMapping("/{noticeId}")
    fun update(
        @PathVariable noticeId: Long,
        @RequestParam type: NoticeType,
        @RequestParam title: String,
        @RequestParam content: String,
    ): String {
        noticeService.update(
            noticeId = noticeId,
            type = type,
            title = title,
            content = content
        )

        return "redirect:/notices"
    }

    @PostMapping("/{noticeId}/hide")
    fun hide(@PathVariable noticeId: Long): String {
        noticeService.hide(noticeId)

        return "redirect:/notices"
    }

    @PostMapping("/{noticeId}/restore")
    fun restore(@PathVariable noticeId: Long): String {
        noticeService.restore(noticeId)

        return "redirect:/notices"
    }
}
