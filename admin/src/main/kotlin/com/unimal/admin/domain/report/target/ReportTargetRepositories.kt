package com.unimal.admin.domain.report.target

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 신고 대상 조회용 리포지토리들.
 *
 * 전부 읽기 전용 엔티티라 저장 메서드를 쓸 일이 없다. 파일을 나누기보다 한곳에 모아
 * "이 묶음은 신고 대상을 보여주려고만 있다" 는 게 드러나게 둔다.
 */

interface ReportedBoardRepository : JpaRepository<ReportedBoard, Long>

interface ReportedBoardFileRepository : JpaRepository<ReportedBoardFile, Long> {

    /** 대표 이미지 우선(main desc), 그다음 등록순. */
    fun findByBoardIdOrderByMainDescIdAsc(boardId: Long): List<ReportedBoardFile>
}

interface ReportedReplyRepository : JpaRepository<ReportedReply, Long>

interface ReportedBoardMemberRepository : JpaRepository<ReportedBoardMember, Long> {

    /**
     * 이메일로 찾는다.
     *
     * 게시글·댓글이 작성자를 이메일로 들고 있고(`board.email`, `board_reply.email`),
     * 이메일은 `board_member` 에 unique 제약이 걸려 있어 단건이 보장된다.
     */
    fun findByEmail(email: String): ReportedBoardMember?
}
