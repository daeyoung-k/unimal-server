package com.unimal.admin.domain.board

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 게시판 관리 조회용 리포지토리들.
 *
 * 엔티티는 읽기 전용이고, 유일한 쓰기 경로는 [BoardPostRepository.updateShow] 하나다.
 * 회원 관리([com.unimal.admin.domain.appmember.AppMemberRepository])와 같은 방식 —
 * `@Immutable` 엔티티는 더티 체킹으로 못 고치므로, 허용된 조치만 네이티브 쿼리로
 * 뚫어 둔다. 조치 범위가 쿼리 시그니처로 드러나는 것이 오히려 장점이다.
 */

interface BoardPostRepository :
    JpaRepository<BoardPost, Long>,
    JpaSpecificationExecutor<BoardPost> {

    /**
     * 공개 상태 변경 (블락/해제 전용).
     *
     * `updated_at` 은 건드리지 않는다 — 그 컬럼은 작성자의 수정 시각이다.
     * 조치 시각은 `admin_board_action_log.created_at` 에 남는다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            update unimal_board.board
            set show = :show
            where id = :id
        """,
        nativeQuery = true
    )
    fun updateShow(
        @Param("id") id: Long,
        @Param("show") show: String,
    ): Int
}

interface BoardPostFileRepository : JpaRepository<BoardPostFile, Long> {

    /** 대표 이미지 우선(main desc), 그다음 등록순. */
    fun findByBoardIdOrderByMainDescIdAsc(boardId: Long): List<BoardPostFile>

    /**
     * 목록 한 페이지의 이미지를 **한 번에** 가져온다.
     *
     * 행마다 따로 조회하면 페이지 크기만큼 쿼리가 나간다(N+1). 어드민이라 치명적이진
     * 않지만, 메서드 하나로 막을 수 있는 낭비다. 정렬이 boardId → main 순이라
     * boardId 로 그룹핑하면 각 그룹의 첫 번째가 대표 이미지다.
     */
    fun findByBoardIdInOrderByBoardIdAscMainDescIdAsc(boardIds: Collection<Long>): List<BoardPostFile>
}
