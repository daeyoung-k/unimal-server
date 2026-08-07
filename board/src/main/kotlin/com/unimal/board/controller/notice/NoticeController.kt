package com.unimal.board.controller.notice

import com.unimal.board.service.notice.NoticeService
import com.unimal.common.dto.CommonResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 앱용 공지 조회.
 *
 * **조회 전용이다. 작성·수정은 어드민(`/notices`)에서만 한다.**
 *
 * 이 라우트는 게이트웨이의 `boardPublicRoutes` 에 있어 인증 필터가 붙지 않는다
 * (공지는 비로그인도 봐야 한다). 그래서 여기에 쓰기 엔드포인트를 두면 **누구나
 * 앱 공지사항에 글을 올릴 수 있다** — 실제로 `POST /notice` 가 그 상태로 열려
 * 있었다(2026-08-07 제거). 공지는 앱에서 운영자의 말로 읽히는 자리라 피싱에도
 * 쓰일 수 있다.
 *
 * 여기에 다시 쓰기 메서드를 추가하지 않는다. 필요하면 어드민에 만든다.
 */
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
}
