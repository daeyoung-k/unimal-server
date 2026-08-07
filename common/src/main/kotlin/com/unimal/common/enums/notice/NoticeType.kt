package com.unimal.common.enums.notice

/**
 * 공지사항 분류.
 *
 * **`common` 에 두는 이유.** 공지는 `board` 가 앱에 내려주고 `admin` 이 작성·수정한다.
 * 두 모듈이 같은 테이블(`unimal_board.notice`)을 보므로 분류 값도 같아야 한다.
 * 한쪽에만 두고 다른 쪽에서 복제하면 값이 조용히 어긋난다 — 신고 관련 enum
 * ([com.unimal.common.enums.report.ReportStatus] 등)이 이미 같은 이유로 여기 있다.
 *
 * [description] 은 어드민 화면의 선택지 라벨로 쓴다. 앱에는 `name` 이 그대로 나간다.
 */
enum class NoticeType(
    val description: String
) {
    NOTICE("일반 공지"),
    EVENT("이벤트"),
    UPDATE("업데이트"),
    MAINTENANCE("점검"),
    POLICY("정책"),
    GUIDE("이용 안내"),
    CAMPAIGN("캠페인")
}
