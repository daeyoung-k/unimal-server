package com.unimal.admin.domain.notice

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.notice.NoticeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 공지사항 — 어드민 쪽 정의.
 *
 * **`board` 모듈에도 같은 테이블을 보는 `Notice` 엔티티가 따로 있다.** 일부러 그렇게 둔다.
 *
 * - `board` 의 것은 **앱에 내려주기 위한 읽기 모델**이라 모든 필드가 `val` 이고
 *   `toResponse()` 로 앱 DTO 를 만든다.
 * - 여기 것은 **작성·수정을 위한 쓰기 모델**이라 `var` 와 도메인 메서드를 갖는다.
 *
 * 하나로 합치면 엔티티를 `common` 에 올려야 하는데, 그러면 공용 라이브러리가 게시판
 * 도메인을 알게 되고 한쪽 요구로 필드를 바꿀 때마다 다른 쪽이 같이 흔들린다.
 * 신고([com.unimal.admin.domain.report.Report])가 이미 같은 방식으로 나뉘어 있다.
 *
 * 대신 **분류값만은 [NoticeType] 으로 공유한다** — 값이 어긋나면 앱이 모르는 타입을
 * 받게 되기 때문이다.
 */
@Entity
@Table(name = "notice", schema = "unimal_board")
open class Notice(
    @Enumerated(EnumType.STRING)
    var type: NoticeType,

    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String,

    /**
     * 앱 노출 여부. `false` 면 목록·상세에서 사라진다.
     *
     * **삭제 대신 이 값을 내린다.** 공지는 푸시(`NOTICE` 타입)로 이미 나갔을 수 있고,
     * 사용자 기기에 남은 알림을 누르면 상세로 들어온다. 행을 지우면 그 경로가 500 이
     * 되지만 숨김이면 "없는 공지" 로 자연스럽게 처리된다. 잘못 올린 공지를 되살릴
     * 수 있다는 점도 크다.
     */
    var show: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null,
) : BaseIdEntity() {

    fun update(type: NoticeType, title: String, content: String) {
        this.type = type
        this.title = title
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    /** 앱에서 감춘다. [show] KDoc 참고 — 이것이 이 화면의 "삭제" 다. */
    fun hide() {
        this.show = false
        this.updatedAt = LocalDateTime.now()
    }

    fun restore() {
        this.show = true
        this.updatedAt = LocalDateTime.now()
    }
}
