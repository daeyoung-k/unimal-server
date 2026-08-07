package com.unimal.admin.domain.report.target

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.hibernate.annotations.Immutable

/**
 * 신고된 게시글 — **읽기 전용**.
 *
 * `board` 모듈의 `Board` 엔티티와 같은 테이블을 보지만, 어드민은 신고 판단에 필요한
 * 값만 읽으면 되므로 위치(PostGIS `Point`)·이미지 연관관계 같은 무거운 매핑을 들이지
 * 않는다. `@Immutable` 을 붙여 **어드민이 실수로 게시글을 수정하는 경로 자체를 없앤다**
 * — 제재는 회원 관리 화면에서 사유를 남기고 하는 것이 원칙이다.
 *
 * 작성자는 `email` 컬럼(문자열)로만 들고 온다. `board_member` 와의 연관관계를 걸면
 * 조회 한 번에 테이블이 셋 엮이는데, 화면에는 이메일과 닉네임만 필요하다.
 */
@Entity
@Immutable
@Table(name = "board", schema = "unimal_board")
open class ReportedBoard(
    @Column(name = "email", insertable = false, updatable = false)
    val email: String = "",

    val title: String? = null,

    @Column(columnDefinition = "text")
    val content: String = "",

    val streetName: String? = null,
    val dong: String? = null,

    /** `PostShow` 값. 어드민에서는 표시만 하므로 문자열로 둔다. */
    @Column(name = "show", length = 20)
    val show: String = "",

    val del: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
) : BaseIdEntity()
