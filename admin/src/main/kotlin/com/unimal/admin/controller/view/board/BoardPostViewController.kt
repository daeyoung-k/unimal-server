package com.unimal.admin.controller.view.board

import com.unimal.admin.service.board.BoardPostSearchCondition
import com.unimal.admin.service.board.BoardPostService
import com.unimal.admin.service.board.BoardPostShow
import com.unimal.admin.service.board.BoardPostSort
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 게시판 관리 화면.
 *
 * 목록에서 전체 게시글을 훑고, 상세에서 본문·사진·위치를 확인한다.
 * 게시글에 대한 조치는 블락/해제뿐이고 반드시 사유를 남긴다.
 * 회원 자체에 대한 제재는 회원 관리에서 진행한다.
 */
@Controller
@RequestMapping("/boards")
class BoardPostViewController(
    private val boardPostService: BoardPostService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) show: String? = null,
        @RequestParam(required = false) del: String? = null,
        @RequestParam(required = false) hasImage: String? = null,
        @RequestParam(required = false) keyword: String? = null,
        @RequestParam(defaultValue = "latest") sort: String = "latest",
        model: Model
    ): String {
        val condition = BoardPostSearchCondition(
            show = BoardPostSearchCondition.normalizeShow(show),
            del = BoardPostSearchCondition.normalizeFlag(del),
            hasImage = BoardPostSearchCondition.normalizeFlag(hasImage),
            keyword = keyword,
            sort = BoardPostSort.from(sort)
        ).normalized()

        val boardsPage = boardPostService.getBoards(page, size, condition)

        model.addAttribute("boardsPage", boardsPage)
        model.addAttribute("imageSummaries", boardPostService.getImageSummaries(boardsPage.content))
        model.addAttribute("page", boardsPage.number)
        model.addAttribute("size", boardsPage.size)
        model.addAttribute("filter", condition)
        model.addAttribute("showTypes", BoardPostShow.entries)
        // 배지 표기용. 템플릿에서 T() 정적 호출은 Thymeleaf 3.1 부터 막혀 있어 맵으로 넘긴다.
        model.addAttribute("showDescriptions", BoardPostShow.entries.associate { it.name to it.description })
        model.addAttribute("sortOptions", BoardPostSort.entries)

        return "board/list"
    }

    @GetMapping("/{boardId}")
    fun detail(
        @PathVariable boardId: Long,
        model: Model
    ): String {
        val board = boardPostService.getBoard(boardId)

        model.addAttribute("board", board)
        model.addAttribute("images", boardPostService.getImages(boardId))
        model.addAttribute("showLabel", BoardPostShow.describe(board.show))
        model.addAttribute("isBlocked", board.show == BoardPostShow.BLOCKED.name)
        model.addAttribute("actionLogs", boardPostService.getActionLogs(boardId))

        return "board/detail"
    }

    @PostMapping("/{boardId}/block")
    fun block(
        @PathVariable boardId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        boardPostService.block(
            boardId = boardId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/boards/$boardId"
    }

    @PostMapping("/{boardId}/unblock")
    fun unblock(
        @PathVariable boardId: Long,
        @RequestParam reason: String,
        authentication: Authentication
    ): String {
        boardPostService.unblock(
            boardId = boardId,
            adminLoginId = authentication.name,
            reason = reason
        )

        return "redirect:/boards/$boardId"
    }
}
