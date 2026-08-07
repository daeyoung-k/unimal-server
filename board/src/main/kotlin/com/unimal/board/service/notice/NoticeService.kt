package com.unimal.board.service.notice

import com.unimal.board.controller.notice.dto.NoticeResponse
import com.unimal.board.domain.notice.NoticeRepository
import com.unimal.webcommon.exception.NoticeNotFoundException
import org.springframework.stereotype.Service

/**
 * 앱용 공지 조회.
 *
 * 작성·수정은 어드민이 담당한다 — [com.unimal.board.controller.notice.NoticeController] KDoc 참고.
 */
@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {

    /**
     * 공지 단건.
     *
     * **숨긴 공지(`show = false`)는 없는 것으로 처리한다.** 목록에서만 빼면 부족하다 —
     * `NOTICE` 타입 푸시를 받은 기기에는 알림이 남아 있고, 그걸 누르면 상세로 바로
     * 들어온다. 잘못 올려서 내린 공지가 그 경로로 계속 읽히면 숨김의 의미가 없다.
     *
     * 숫자가 아닌 `id` 도 같은 예외로 흘린다. `toLong()` 을 그대로 쓰면 500 이 되고,
     * 앱이 잘못된 딥링크를 한 번 열 때마다 에러 로그가 쌓인다.
     */
    fun getNotice(
        id: String
    ): NoticeResponse {
        val noticeId = id.toLongOrNull() ?: throw NoticeNotFoundException()

        return noticeRepository.findById(noticeId)
            .orElseThrow { NoticeNotFoundException() }
            .takeIf { it.show }
            ?.toResponse()
            ?: throw NoticeNotFoundException()
    }

    fun getNoticeList(): List<NoticeResponse> {
        return noticeRepository.findByShowOrderByIdDesc().map {
            it.toResponse()
        }
    }
}