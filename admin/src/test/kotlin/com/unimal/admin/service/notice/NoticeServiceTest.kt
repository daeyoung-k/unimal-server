package com.unimal.admin.service.notice

import com.unimal.admin.domain.notice.Notice
import com.unimal.admin.domain.notice.NoticeRepository
import com.unimal.common.enums.notice.NoticeType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

class NoticeServiceTest {

    private val noticeRepository = mockk<NoticeRepository>()
    private val noticeService = NoticeService(noticeRepository)

    private fun notice(
        type: NoticeType = NoticeType.NOTICE,
        title: String = "점검 안내",
        content: String = "오늘 밤 서비스 점검이 있습니다.",
        show: Boolean = true,
    ) = Notice(type = type, title = title, content = content, show = show)

    @Test
    fun `목록은 최신순 페이징으로 조회한다`() {
        val pageableSlot = slot<Pageable>()

        every {
            noticeRepository.findAll(any<Specification<Notice>>(), capture(pageableSlot))
        } returns PageImpl(listOf(notice()))

        noticeService.getNotices(page = 0, size = 20)

        verify { noticeRepository.findAll(any<Specification<Notice>>(), any<Pageable>()) }
        assertEquals(Sort.by(Sort.Direction.DESC, "id"), pageableSlot.captured.sort)
    }

    @Test
    fun `페이지 크기는 1에서 100 사이로 조정된다`() {
        // 쿼리 파라미터로 size=99999 가 들어오면 어드민이 통째로 로딩된다.
        val pageableSlot = slot<Pageable>()

        every {
            noticeRepository.findAll(any<Specification<Notice>>(), capture(pageableSlot))
        } returns PageImpl(emptyList())

        noticeService.getNotices(page = -5, size = 99999)

        assertEquals(0, pageableSlot.captured.pageNumber)
        assertEquals(100, pageableSlot.captured.pageSize)
    }

    @Test
    fun `생성 시 제목과 내용의 앞뒤 공백을 제거한다`() {
        val noticeSlot = slot<Notice>()

        every { noticeRepository.save(capture(noticeSlot)) } answers { noticeSlot.captured }

        noticeService.create(
            type = NoticeType.EVENT,
            title = "  이벤트 안내  ",
            content = "\n본문입니다.\n"
        )

        assertEquals("이벤트 안내", noticeSlot.captured.title)
        assertEquals("본문입니다.", noticeSlot.captured.content)
        assertEquals(NoticeType.EVENT, noticeSlot.captured.type)
        assertTrue(noticeSlot.captured.show, "새 공지는 노출 상태로 만들어져야 한다")
    }

    @Test
    fun `새로 만든 공지의 수정일은 비어 있다`() {
        // 목록에서 "수정된 적 있는 공지" 를 구분하는 근거라 생성 시점에 채우면 안 된다.
        val noticeSlot = slot<Notice>()

        every { noticeRepository.save(capture(noticeSlot)) } answers { noticeSlot.captured }

        noticeService.create(type = NoticeType.NOTICE, title = "제목", content = "내용")

        assertNull(noticeSlot.captured.updatedAt)
    }

    @Test
    fun `수정하면 내용과 수정일이 함께 갱신된다`() {
        val existing = notice(title = "옛 제목", content = "옛 내용")

        every { noticeRepository.findById(1L) } returns Optional.of(existing)

        val updated = noticeService.update(
            noticeId = 1L,
            type = NoticeType.UPDATE,
            title = "새 제목",
            content = "새 내용"
        )

        assertEquals("새 제목", updated.title)
        assertEquals("새 내용", updated.content)
        assertEquals(NoticeType.UPDATE, updated.type)
        assertNotNull(updated.updatedAt, "수정일이 채워지지 않았다")
    }

    @Test
    fun `숨김은 행을 지우지 않고 show 만 내린다`() {
        // 푸시로 나간 공지의 상세 진입이 500 이 되지 않게 하려는 것이므로
        // delete 가 호출되면 안 된다.
        val existing = notice()

        every { noticeRepository.findById(1L) } returns Optional.of(existing)

        noticeService.hide(1L)

        assertFalse(existing.show)
        // JpaSpecificationExecutor 가 delete(Specification) 를 함께 물고 오므로
        // 타입을 명시해야 어느 오버로드인지 정해진다.
        verify(exactly = 0) { noticeRepository.delete(any<Notice>()) }
        verify(exactly = 0) { noticeRepository.deleteById(any()) }
    }

    @Test
    fun `복구하면 다시 노출된다`() {
        val existing = notice(show = false)

        every { noticeRepository.findById(1L) } returns Optional.of(existing)

        noticeService.restore(1L)

        assertTrue(existing.show)
    }

    @Test
    fun `없는 공지를 조회하면 예외가 난다`() {
        every { noticeRepository.findById(404L) } returns Optional.empty()

        val error = runCatching { noticeService.getNotice(404L) }.exceptionOrNull()

        assertTrue(error is NoSuchElementException, "실제 예외: $error")
    }
}
