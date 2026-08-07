package com.unimal.admin.service.report

import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReportSearchConditionTest {

    @Test
    fun `상태 파라미터가 없으면 미처리만 보여준다`() {
        // 신고 관리에서 할 일은 "아직 처리 안 한 것 처리하기" 다.
        // 첫 진입에 전체가 나오면 매번 필터를 다시 걸어야 한다.
        assertEquals(ReportStatus.PENDING, ReportSearchCondition.normalizeStatus(null))
    }

    @Test
    fun `빈 문자열은 전체 조회로 해석한다`() {
        // null(파라미터 없음)과 ""("전체" 선택)를 구분해야 한다.
        // 둘을 같게 처리하면 "전체 보기" 를 눌러도 미처리만 나온다.
        assertNull(ReportSearchCondition.normalizeStatus(""))
        assertNull(ReportSearchCondition.normalizeStatus("   "))
    }

    @Test
    fun `상태값은 대소문자를 가리지 않는다`() {
        assertEquals(ReportStatus.RESOLVED, ReportSearchCondition.normalizeStatus("resolved"))
        assertEquals(ReportStatus.RESOLVED, ReportSearchCondition.normalizeStatus("RESOLVED"))
        assertNull(ReportSearchCondition.normalizeStatus("없는상태"))
    }

    @Test
    fun `대상 유형과 사유도 해석한다`() {
        assertEquals(ReportTargetType.POST, ReportSearchCondition.normalizeTargetType("post"))
        assertNull(ReportSearchCondition.normalizeTargetType(""))
        assertEquals(ReportReason.SPAM, ReportSearchCondition.normalizeReason("spam"))
        assertNull(ReportSearchCondition.normalizeReason("없는사유"))
    }

    @Test
    fun `기본 정렬은 오래된순이다`() {
        // 신고는 처리 대기열이라 먼저 들어온 것부터 봐야 한다.
        // 최신순이면 오래된 신고가 목록 뒤로 밀려 영영 남는다.
        assertEquals(ReportSort.OLDEST, ReportSort.from(null))
        assertEquals(ReportSort.OLDEST, ReportSort.from("이상한값"))
        assertEquals(ReportSort.LATEST, ReportSort.from("latest"))
    }

    @Test
    fun `검색 조건의 기본값도 미처리다`() {
        assertEquals(ReportStatus.PENDING, ReportSearchCondition().status)
        assertEquals(ReportSort.OLDEST, ReportSearchCondition().sort)
    }
}
