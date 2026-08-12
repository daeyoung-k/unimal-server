package com.unimal.admin.domain.board

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 게시글 첨부 이미지 — **읽기 전용**.
 *
 * 게시판 관리에서는 목록에 대표 썸네일을, 상세에 전체 사진을 보여준다.
 * 텍스트만 보고는 어떤 게시글인지 감이 안 오고, 문제 이미지(음란물·광고)는
 * 사진을 봐야만 찾을 수 있다.
 *
 * [com.unimal.admin.domain.report.target.ReportedBoardFile] 과 같은 테이블을 보지만
 * 신고 묶음과 분리해 둔다 — 매핑 정의는 그쪽과 동일하게 유지할 것.
 */
@Entity
@Immutable
@Table(name = "board_file", schema = "unimal_board")
open class BoardPostFile(
    @Column(name = "board_id")
    val boardId: Long = 0,

    val main: Boolean = false,

    val fileUrl: String? = null,

    val thumbUrl: String? = null,
) : BaseIdEntity() {

    /** 화면 표시용 URL. 썸네일 우선, 백필 전 파일은 원본 폴백. */
    val displayUrl: String?
        get() = thumbUrl ?: fileUrl
}
