package com.unimal.board.service.notice

import com.unimal.board.domain.notice.Notice
import com.unimal.board.domain.notice.NoticeRepository
import com.unimal.common.enums.notice.NoticeType
import com.unimal.webcommon.exception.NoticeNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 앱용 공지 조회.
 *
 * **숨김 처리가 조회 경로 전부에서 지켜지는지에 집중한다.** 목록에서만 빼고 상세를
 * 열어두면, 잘못 올려서 내린 공지가 푸시 알림을 통해 계속 읽힌다 — 어드민의 숨김
 * 버튼이 무력해지는데 화면상으로는 정상으로 보여서 알아채기 어렵다.
 */
class NoticeServiceTest {

    private val noticeRepository = mockk<NoticeRepository>()
    private val noticeService = NoticeService(noticeRepository)

    private fun notice(show: Boolean = true) = Notice(
        type = NoticeType.NOTICE,
        title = "점검 안내",
        content = "오늘 밤 서비스 점검이 있습니다.",
        show = show,
    ).apply { id = 1L }

    @Test
    fun `노출중인 공지는 상세로 조회된다`() {
        every { noticeRepository.findById(1L) } returns Optional.of(notice())

        val response = noticeService.getNotice("1")

        assertEquals("점검 안내", response.title)
    }

    @Test
    fun `숨긴 공지는 상세에서도 없는 것으로 처리된다`() {
        // 푸시 알림에 남은 링크로 들어오는 경로를 막는다.
        every { noticeRepository.findById(1L) } returns Optional.of(notice(show = false))

        val error = runCatching { noticeService.getNotice("1") }.exceptionOrNull()

        assertTrue(error is NoticeNotFoundException, "실제 예외: $error")
    }

    @Test
    fun `숫자가 아닌 id 는 조회를 시도하지 않고 없는 공지로 처리한다`() {
        // toLong() 을 그대로 쓰면 NumberFormatException 이 500 으로 나가고
        // 잘못된 딥링크 한 번에 에러 로그가 쌓인다.
        val error = runCatching { noticeService.getNotice("abc") }.exceptionOrNull()

        assertTrue(error is NoticeNotFoundException, "실제 예외: $error")
        verify(exactly = 0) { noticeRepository.findById(any()) }
    }

    @Test
    fun `없는 공지는 예외가 난다`() {
        every { noticeRepository.findById(404L) } returns Optional.empty()

        val error = runCatching { noticeService.getNotice("404") }.exceptionOrNull()

        assertTrue(error is NoticeNotFoundException, "실제 예외: $error")
    }

    @Test
    fun `목록은 노출중인 공지만 내려준다`() {
        every { noticeRepository.findByShowOrderByIdDesc(true) } returns listOf(notice())

        val responses = noticeService.getNoticeList()

        assertEquals(1, responses.size)
        verify { noticeRepository.findByShowOrderByIdDesc(true) }
    }
}
