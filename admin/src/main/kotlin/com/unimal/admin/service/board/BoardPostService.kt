package com.unimal.admin.service.board

import com.unimal.admin.domain.adminmember.AdminMember
import com.unimal.admin.domain.adminmember.AdminMemberRepository
import com.unimal.admin.domain.board.BoardPost
import com.unimal.admin.domain.board.BoardPostFile
import com.unimal.admin.domain.board.BoardPostFileRepository
import com.unimal.admin.domain.board.BoardPostRepository
import com.unimal.admin.domain.board.actionlog.BoardPostActionLog
import com.unimal.admin.domain.board.actionlog.BoardPostActionLogRepository
import com.unimal.admin.domain.board.actionlog.BoardPostActionType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 게시판 관리.
 *
 * 게시글 생성·수정은 앱에서 `board` 서비스가 한다. 여기서 허용되는 조치는
 * **블락/해제 딱 둘**이고, 반드시 사유와 함께 로그를 남긴다. 본문·제목을 고치는
 * 메서드를 추가하고 싶어지면 엔티티가 `@Immutable` 인 이유부터 다시 읽을 것.
 *
 * 블락하면 앱의 목록·지도 쿼리(`show = 'PUBLIC'` 등호 비교)에서 즉시 빠진다.
 * 지도 피드 캐시(TTL 60초)에는 최대 1분 남을 수 있는데, 강제 무효화까지 할 만큼
 * 급한 조치가 아니라 그냥 둔다.
 */
@Service
class BoardPostService(
    private val boardPostRepository: BoardPostRepository,
    private val boardPostFileRepository: BoardPostFileRepository,
    private val adminMemberRepository: AdminMemberRepository,
    private val boardPostActionLogRepository: BoardPostActionLogRepository,
) {

    @Transactional(readOnly = true)
    fun getBoards(
        page: Int,
        size: Int,
        condition: BoardPostSearchCondition = BoardPostSearchCondition()
    ): Page<BoardPost> {
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            condition.sort.toSort()
        )

        return boardPostRepository.findAll(condition.toSpecification(), pageable)
    }

    @Transactional(readOnly = true)
    fun getBoard(boardId: Long): BoardPost {
        return boardPostRepository.findById(boardId)
            .orElseThrow { NoSuchElementException("Board not found: $boardId") }
    }

    /** 상세 화면용 전체 이미지. 대표 이미지 우선. */
    @Transactional(readOnly = true)
    fun getImages(boardId: Long): List<BoardPostFile> =
        boardPostFileRepository.findByBoardIdOrderByMainDescIdAsc(boardId)

    /**
     * 목록 한 페이지의 이미지 요약(대표 썸네일 + 장수)을 만든다.
     *
     * 페이지 전체 이미지를 쿼리 한 번으로 가져와 boardId 로 묶는다. 정렬이
     * boardId → main desc 순이라 각 그룹의 첫 번째가 대표 이미지다.
     */
    @Transactional(readOnly = true)
    fun getImageSummaries(boards: List<BoardPost>): Map<Long, BoardPostImageSummary> {
        val boardIds = boards.mapNotNull { it.id }
        if (boardIds.isEmpty()) return emptyMap()

        return boardPostFileRepository
            .findByBoardIdInOrderByBoardIdAscMainDescIdAsc(boardIds)
            .groupBy { it.boardId }
            .mapValues { (_, files) ->
                BoardPostImageSummary(
                    thumbnailUrl = files.first().displayUrl,
                    count = files.size,
                )
            }
    }

    @Transactional(readOnly = true)
    fun getActionLogs(boardId: Long): List<BoardPostActionLog> =
        boardPostActionLogRepository.findTop20ByBoardIdOrderByCreatedAtDescIdDesc(boardId)

    @Transactional
    fun block(boardId: Long, adminLoginId: String, reason: String) {
        val board = getBoard(boardId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)

        require(board.show != BoardPostShow.BLOCKED.name) { "Board is already blocked: $boardId" }

        val updatedCount = boardPostRepository.updateShow(boardId, BoardPostShow.BLOCKED.name)

        require(updatedCount == 1) { "Failed to block board: $boardId" }
        recordActionLog(
            adminMember = adminMember,
            board = board,
            actionType = BoardPostActionType.BOARD_BLOCK,
            reason = normalizedReason,
            beforeValue = board.show,
            afterValue = BoardPostShow.BLOCKED.name
        )
    }

    /**
     * 블락 해제 — 블락 직전 상태로 되돌린다.
     *
     * `PUBLIC` 으로 일괄 복구하면 원래 `PRIVATE` 였던 글이 강제 공개되는 사고가 난다.
     * 이전 값은 블락 로그의 `beforeValue` 에만 남아 있으므로 거기서 찾고,
     * 로그가 없으면(수동 DB 조작 등) 가장 보수적인 `PRIVATE` 로 되돌린다.
     */
    @Transactional
    fun unblock(boardId: Long, adminLoginId: String, reason: String) {
        val board = getBoard(boardId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)

        require(board.show == BoardPostShow.BLOCKED.name) { "Only blocked board can be unblocked: $boardId" }

        val restoredShow = boardPostActionLogRepository
            .findTopByBoardIdAndActionTypeOrderByIdDesc(boardId, BoardPostActionType.BOARD_BLOCK)
            ?.beforeValue
            ?: BoardPostShow.PRIVATE.name

        val updatedCount = boardPostRepository.updateShow(boardId, restoredShow)

        require(updatedCount == 1) { "Failed to unblock board: $boardId" }
        recordActionLog(
            adminMember = adminMember,
            board = board,
            actionType = BoardPostActionType.BOARD_UNBLOCK,
            reason = normalizedReason,
            beforeValue = BoardPostShow.BLOCKED.name,
            afterValue = restoredShow
        )
    }

    private fun getAdminMember(adminLoginId: String): AdminMember {
        return adminMemberRepository.findByLoginId(adminLoginId)
            ?: throw NoSuchElementException("Admin member not found: $adminLoginId")
    }

    private fun normalizeReason(reason: String): String {
        return reason.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Action reason is required")
    }

    private fun recordActionLog(
        adminMember: AdminMember,
        board: BoardPost,
        actionType: BoardPostActionType,
        reason: String,
        beforeValue: String?,
        afterValue: String?,
    ) {
        boardPostActionLogRepository.save(
            BoardPostActionLog(
                adminMemberId = requireNotNull(adminMember.id),
                adminLoginId = adminMember.loginId,
                boardId = requireNotNull(board.id),
                boardEmail = board.email,
                actionType = actionType,
                reason = reason,
                beforeValue = beforeValue,
                afterValue = afterValue
            )
        )
    }

    private fun BoardPostSearchCondition.toSpecification(): Specification<BoardPost> =
        Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            show?.let {
                predicates.add(criteriaBuilder.equal(root.get<String>("show"), it.name))
            }

            del?.let {
                predicates.add(criteriaBuilder.equal(root.get<Boolean>("del"), it))
            }

            hasImage?.let { wantImage ->
                // 사진 유무는 board_file 존재 여부다. exists 서브쿼리라 목록 쿼리와
                // count 쿼리 양쪽에서 그대로 동작한다 (join 이면 count 가 이미지 수만큼 부푼다).
                val subquery = requireNotNull(query).subquery(Long::class.java)
                val file = subquery.from(BoardPostFile::class.java)
                subquery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(file.get<Long>("boardId"), root.get<Long>("id")))

                val exists = criteriaBuilder.exists(subquery)
                predicates.add(if (wantImage) exists else criteriaBuilder.not(exists))
            }

            keyword?.let {
                val pattern = "%${it.lowercase()}%"
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                    )
                )
            }

            if (predicates.isEmpty()) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
}

/** 목록 행에 필요한 만큼만 — 대표 썸네일과 사진 장수. */
data class BoardPostImageSummary(
    val thumbnailUrl: String?,
    val count: Int,
)
