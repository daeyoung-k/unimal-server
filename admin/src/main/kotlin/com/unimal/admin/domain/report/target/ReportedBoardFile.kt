package com.unimal.admin.domain.report.target

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * 신고된 게시글의 첨부 이미지 — **읽기 전용**.
 *
 * 음란물·혐오 이미지 신고는 **사진을 봐야 판단할 수 있다.** 본문만 보고 처리하면
 * 정작 문제가 되는 부분을 놓친다.
 *
 * 어드민 화면에서는 [thumbUrl] 을 우선 쓴다. 원본은 크고, 판단에는 썸네일로 충분하다.
 */
@Entity
@Immutable
@Table(name = "board_file", schema = "unimal_board")
open class ReportedBoardFile(
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
