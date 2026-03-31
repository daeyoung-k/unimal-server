package com.unimal.board.controller.notice

import com.unimal.board.controller.notice.dto.NoticePostRequest
import com.unimal.board.service.notice.NoticeService
import com.unimal.common.dto.CommonResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notice")
class NoticeController(
    private val noticeService: NoticeService
) {

    @GetMapping("/{id}")
    fun getNotice(
        @PathVariable id: String
    ): CommonResponse {
        return CommonResponse(data = noticeService.getNotice(id))
    }

    @GetMapping
    fun getNoticeList(): CommonResponse {
        return CommonResponse(data = noticeService.getNoticeList())
    }

    @PostMapping
    fun create(
        @RequestBody noticePostRequest: NoticePostRequest
    ): CommonResponse {
        return CommonResponse(data = noticeService.createNotice(noticePostRequest))
    }
}