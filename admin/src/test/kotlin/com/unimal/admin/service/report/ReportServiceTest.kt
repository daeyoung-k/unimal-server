package com.unimal.admin.service.report

import com.unimal.admin.domain.report.Report
import com.unimal.admin.domain.report.ReportRepository
import com.unimal.admin.domain.report.target.ReportedBoard
import com.unimal.admin.domain.report.target.ReportedBoardFile
import com.unimal.admin.domain.report.target.ReportedBoardFileRepository
import com.unimal.admin.domain.report.target.ReportedBoardMember
import com.unimal.admin.domain.report.target.ReportedBoardMemberRepository
import com.unimal.admin.domain.report.target.ReportedBoardRepository
import com.unimal.admin.domain.report.target.ReportedReply
import com.unimal.admin.domain.report.target.ReportedReplyRepository
import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportServiceTest {

    private val reportRepository = mockk<ReportRepository>()
    private val reportedBoardRepository = mockk<ReportedBoardRepository>()
    private val reportedBoardFileRepository = mockk<ReportedBoardFileRepository>()
    private val reportedReplyRepository = mockk<ReportedReplyRepository>()
    private val reportedBoardMemberRepository = mockk<ReportedBoardMemberRepository>()

    private val reportService = ReportService(
        reportRepository,
        reportedBoardRepository,
        reportedBoardFileRepository,
        reportedReplyRepository,
        reportedBoardMemberRepository,
    )

    private fun report(
        targetType: ReportTargetType = ReportTargetType.POST,
        targetId: Long = 100L,
        status: ReportStatus = ReportStatus.PENDING,
    ) = Report(
        reporterEmail = "reporter@unimal.co.kr",
        targetType = targetType,
        targetId = targetId,
        reason = ReportReason.ABUSE,
        description = "욕설이 심합니다",
        status = status,
    ).apply { id = 1L }

    private fun board(email: String = "author@unimal.co.kr") = ReportedBoard(
        email = email,
        title = "제목",
        content = "본문",
        dong = "역삼동",
        show = "PUBLIC",
    ).apply { id = 100L }

    // ── 대상 해석 ─────────────────────────────────────────────

    @Test
    fun `게시글 신고는 본문과 이미지, 작성자를 함께 가져온다`() {
        // 음란물·혐오 이미지 신고는 사진을 봐야 판단할 수 있다.
        val image = ReportedBoardFile(boardId = 100L, main = true, thumbUrl = "https://cdn/t.jpg")

        every { reportedBoardRepository.findById(100L) } returns Optional.of(board())
        every { reportedBoardFileRepository.findByBoardIdOrderByMainDescIdAsc(100L) } returns listOf(image)
        every { reportedBoardMemberRepository.findByEmail("author@unimal.co.kr") } returns
            ReportedBoardMember(email = "author@unimal.co.kr", nickname = "글쓴이")

        val target = reportService.resolveTarget(report())

        assertTrue(target is ReportTargetView.Post)
        assertEquals("POST", target.kind)
        assertEquals(1, target.images.size)
        assertEquals("글쓴이", target.author?.nickname)
    }

    @Test
    fun `댓글 신고는 원글도 같이 가져온다`() {
        // 댓글만 보면 맥락을 알 수 없어 판단이 갈린다.
        val reply = ReportedReply(boardId = 100L, email = "author@unimal.co.kr", comment = "댓글")
            .apply { id = 55L }

        every { reportedReplyRepository.findById(55L) } returns Optional.of(reply)
        every { reportedBoardRepository.findById(100L) } returns Optional.of(board())
        every { reportedBoardMemberRepository.findByEmail(any()) } returns null

        val target = reportService.resolveTarget(
            report(targetType = ReportTargetType.REPLY, targetId = 55L)
        )

        assertTrue(target is ReportTargetView.Reply)
        assertNotNull(target.board, "원글을 함께 조회해야 한다")
    }

    @Test
    fun `회원 신고는 board_member 를 targetId 로 조회한다`() {
        // targetId 는 board_member.id 다. unimal_user.member.id 가 아니므로
        // 회원 관리 엔티티를 그대로 조회하면 엉뚱한 사람이 나온다.
        every { reportedBoardMemberRepository.findById(77L) } returns Optional.of(
            ReportedBoardMember(email = "bad@unimal.co.kr", nickname = "신고대상")
        )

        val target = reportService.resolveTarget(
            report(targetType = ReportTargetType.USER, targetId = 77L)
        )

        assertTrue(target is ReportTargetView.User)
        assertEquals("bad@unimal.co.kr", target.member.email)
        verify { reportedBoardMemberRepository.findById(77L) }
    }

    @Test
    fun `삭제된 대상은 예외 대신 Missing 으로 준다`() {
        // 신고 접수 후 작성자가 글을 지우는 건 정상적인 흐름이다.
        // 예외를 던지면 신고 한 건 때문에 화면 전체가 죽는다.
        every { reportedBoardRepository.findById(100L) } returns Optional.empty()

        val target = reportService.resolveTarget(report())

        assertTrue(target is ReportTargetView.Missing)
        assertEquals("MISSING", target.kind)
    }

    // ── 처리 ─────────────────────────────────────────────────

    @Test
    fun `처리하면 상태와 처리자, 처리일이 함께 기록된다`() {
        val existing = report()

        every { reportRepository.findById(1L) } returns Optional.of(existing)

        val reviewed = reportService.review(
            reportId = 1L,
            status = ReportStatus.RESOLVED,
            adminLoginId = "admin",
            memo = "  게시글 숨김 처리함  "
        )

        assertEquals(ReportStatus.RESOLVED, reviewed.status)
        assertEquals("admin", reviewed.reviewedBy)
        assertEquals("게시글 숨김 처리함", reviewed.adminMemo)
        assertNotNull(reviewed.reviewedAt)
    }

    @Test
    fun `빈 메모는 null 로 저장한다`() {
        val existing = report()

        every { reportRepository.findById(1L) } returns Optional.of(existing)

        val reviewed = reportService.review(
            reportId = 1L,
            status = ReportStatus.REJECTED,
            adminLoginId = "admin",
            memo = "   "
        )

        assertNull(reviewed.adminMemo)
    }

    @Test
    fun `처리 상태를 PENDING 으로 지정할 수 없다`() {
        // 처리했는데 여전히 미처리로 남으면 대기열에서 사라지지 않는다.
        val existing = report()

        every { reportRepository.findById(1L) } returns Optional.of(existing)

        val error = runCatching {
            reportService.review(1L, ReportStatus.PENDING, "admin", null)
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException, "실제 예외: $error")
    }

    @Test
    fun `되돌리면 처리자와 처리일이 지워진다`() {
        val existing = report(status = ReportStatus.RESOLVED).apply {
            review(ReportStatus.RESOLVED, "admin", "메모")
        }

        every { reportRepository.findById(1L) } returns Optional.of(existing)

        val reverted = reportService.revertToPending(1L)

        assertEquals(ReportStatus.PENDING, reverted.status)
        assertNull(reverted.reviewedBy)
        assertNull(reverted.reviewedAt)
        // 메모는 남긴다 — 왜 그렇게 판단했는지가 되돌린 뒤에도 필요하다.
        assertEquals("메모", reverted.adminMemo)
    }
}
