package com.unimal.admin.domain.board

import com.unimal.common.domain.BaseIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.hibernate.annotations.Immutable

/**
 * 게시글 — **읽기 전용**.
 *
 * `board` 모듈의 `Board` 엔티티와 같은 테이블을 본다. 어드민 게시판 관리는
 * 운영 현황을 훑고 문제 게시글을 찾아내는 화면이라 조회만 하면 되므로,
 * 위치(PostGIS `Point`)·연관관계 같은 무거운 매핑은 들이지 않는다.
 * `@Immutable` 로 **어드민이 실수로 게시글을 고치는 경로 자체를 없앤다.**
 *
 * [com.unimal.admin.domain.report.target.ReportedBoard] 와 같은 테이블을 보지만
 * 합치지 않는다 — 그쪽은 신고 판단에 필요한 최소 필드만 갖는 신고 전용 묶음이고,
 * 여기는 목록 검색·주소 표시까지 필요하다. 겹치는 컬럼의 매핑 정의는 반드시
 * 그쪽과 동일하게 유지한다 (다르면 스키마 생성 시 충돌한다).
 *
 * 위치는 좌표 대신 역지오코딩된 주소 컬럼(시/도·구/군·동·도로명)으로 보여준다.
 * 화면에서 좌표 숫자는 판단에 도움이 안 되고, PostGIS 함수를 매핑에 끌어들이면
 * H2 기반 테스트가 깨진다.
 */
@Entity
@Immutable
@Table(name = "board", schema = "unimal_board")
open class BoardPost(
    @Column(name = "email", insertable = false, updatable = false)
    val email: String = "",

    val title: String? = null,

    @Column(columnDefinition = "text")
    val content: String = "",

    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,

    /** `PostShow` 값. 어드민에서는 표시·필터만 하므로 문자열로 둔다. */
    @Column(name = "show", length = 20)
    val show: String = "",

    val del: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null,
) : BaseIdEntity() {

    /** 화면 표시용 지역명. 동이 제일 짧고 알아보기 쉽다. 없으면 도로명으로 폴백. */
    val regionLabel: String?
        get() = listOfNotNull(siDo, guGun, dong)
            .joinToString(" ")
            .ifBlank { streetName }
}
