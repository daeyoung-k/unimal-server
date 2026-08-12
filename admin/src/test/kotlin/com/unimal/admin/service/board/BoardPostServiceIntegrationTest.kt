package com.unimal.admin.service.board

import com.unimal.admin.domain.board.actionlog.BoardPostActionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BoardPostServiceIntegrationTest @Autowired constructor(
    private val boardPostService: BoardPostService
) {

    @Test
    fun `boards are filtered by keyword`() {
        val boards = boardPostService.getBoards(
            page = 0,
            size = 20,
            condition = BoardPostSearchCondition(keyword = "역삼동")
        )

        assertEquals(listOf("leaf@unimal.co.kr"), boards.content.map { it.email })
    }

    @Test
    fun `hasImage filter separates photo posts from text posts`() {
        // data.sql — 1번 글만 board_file 이 있다.
        val photoBoards = boardPostService.getBoards(
            page = 0,
            size = 20,
            condition = BoardPostSearchCondition(hasImage = true)
        )
        val textBoards = boardPostService.getBoards(
            page = 0,
            size = 20,
            condition = BoardPostSearchCondition(hasImage = false)
        )

        assertEquals(listOf(1L), photoBoards.content.map { it.id })
        assertEquals(listOf(2L), textBoards.content.map { it.id })
    }

    @Test
    fun `image summaries pick main image first and count all files`() {
        val boards = boardPostService.getBoards(0, 20)
        val summaries = boardPostService.getImageSummaries(boards.content)

        val summary = summaries.getValue(1L)
        assertEquals("https://cdn.unimal.co.kr/board/1/a_thumb.jpg", summary.thumbnailUrl)
        assertEquals(2, summary.count)
        assertEquals(null, summaries[2L])
    }

    /**
     * 블락 → 해제가 한 트랜잭션 흐름으로 도는지, 그리고 해제가 **블락 직전 상태로**
     * 복구하는지 본다. 2번 글은 원래 PRIVATE 라, PUBLIC 일괄 복구 버그가 있으면
     * 여기서 잡힌다. 상태를 원복해 두므로 다른 테스트와 순서가 얽히지 않는다.
     */
    @Test
    fun `block and unblock restore the previous show value and record logs`() {
        boardPostService.block(boardId = 2, adminLoginId = "admin", reason = "신고 확인")

        assertEquals(BoardPostShow.BLOCKED.name, boardPostService.getBoard(2).show)
        // 이미 블락된 글은 다시 블락할 수 없다.
        assertFailsWith<IllegalArgumentException> {
            boardPostService.block(boardId = 2, adminLoginId = "admin", reason = "중복 시도")
        }

        boardPostService.unblock(boardId = 2, adminLoginId = "admin", reason = "오조치 복구")

        assertEquals(BoardPostShow.PRIVATE.name, boardPostService.getBoard(2).show)

        val logs = boardPostService.getActionLogs(2)
        assertEquals(
            listOf(BoardPostActionType.BOARD_UNBLOCK, BoardPostActionType.BOARD_BLOCK),
            logs.map { it.actionType }
        )
        assertEquals("PRIVATE", logs.first { it.actionType == BoardPostActionType.BOARD_BLOCK }.beforeValue)
        assertEquals("PRIVATE", logs.first { it.actionType == BoardPostActionType.BOARD_UNBLOCK }.afterValue)
    }

    @Test
    fun `blank reason is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            boardPostService.block(boardId = 1, adminLoginId = "admin", reason = "   ")
        }
    }
}
