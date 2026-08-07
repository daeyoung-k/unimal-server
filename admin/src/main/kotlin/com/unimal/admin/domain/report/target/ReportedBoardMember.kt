package com.unimal.admin.domain.report.target

import com.unimal.common.domain.BaseIdEntity
import com.unimal.common.enums.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

/**
 * `unimal_board.board_member` — **읽기 전용**.
 *
 * ## 왜 이 엔티티가 따로 필요한가
 *
 * 회원 신고(`targetType = USER`)의 `targetId` 는 **이 테이블의 id** 다.
 * `unimal_user.member.id` 가 아니다.
 *
 * `board_member` 는 `user.signInTopic` 을 소비할 때 id 를 지정하지 않고 저장돼
 * 자체 IDENTITY 시퀀스를 쓴다. 두 테이블의 id 는 우연히 같을 수는 있어도 같다는
 * 보장이 전혀 없다. 이 값으로 곧장 회원 관리의
 * [com.unimal.admin.domain.appmember.AppMember] 를 조회하면 **엉뚱한 회원을 보여주고,
 * 그 사람을 제재하는 사고로 이어진다.**
 *
 * 그래서 신고 대상 회원은 여기서 [email] 을 얻은 뒤, 이메일로 실제 회원을 찾는다.
 * 이메일은 양쪽 테이블에서 같은 값이고 `board_member` 에 unique 제약도 걸려 있다.
 */
@Entity
@Immutable
@Table(name = "board_member", schema = "unimal_board")
open class ReportedBoardMember(
    @Column(length = 50, nullable = false)
    val email: String = "",

    @Column(length = 30)
    val nickname: String? = null,

    val profileImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    val status: UserStatus = UserStatus.ACTIVE,
) : BaseIdEntity()
