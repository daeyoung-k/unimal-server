package com.unimal.board.service.notice

import com.unimal.board.controller.notice.dto.NoticePostRequest
import com.unimal.board.controller.notice.dto.NoticeResponse
import com.unimal.board.domain.notice.NoticeRepository
import com.unimal.webcommon.exception.NoticeNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {

    @Transactional
    fun createNotice(
        noticePostRequest: NoticePostRequest
    ): NoticeResponse {
        val notice = noticeRepository.save(noticePostRequest.toNotice())
        return notice.toResponse()
    }

    fun getNotice(
        id: String
    ): NoticeResponse {
        return noticeRepository.findById(id.toLong()).orElseThrow { throw NoticeNotFoundException() }.let {
            it.toResponse()
        }
    }

    fun getNoticeList(): List<NoticeResponse> {
        return noticeRepository.findByShowOrderByIdDesc().map {
            it.toResponse()
        }
    }
}