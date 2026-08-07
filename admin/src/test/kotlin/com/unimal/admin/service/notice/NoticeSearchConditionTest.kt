package com.unimal.admin.service.notice

import com.unimal.common.enums.notice.NoticeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NoticeSearchConditionTest {

    @Test
    fun `노출 필터의 전체는 null 로 해석한다`() {
        // 여기가 이 클래스에서 가장 틀리기 쉬운 지점이다.
        // "".toBoolean() 은 false 라서, 무심코 toBoolean() 을 쓰면 "전체" 를 고른
        // 사용자에게 "숨김만" 이 보인다. 화면은 멀쩡해 보여서 알아채기 어렵다.
        assertNull(NoticeSearchCondition.normalizeShow(""))
        assertNull(NoticeSearchCondition.normalizeShow(null))
        assertNull(NoticeSearchCondition.normalizeShow("  "))
        assertNull(NoticeSearchCondition.normalizeShow("아무거나"))
    }

    @Test
    fun `노출 필터의 true false 는 그대로 해석한다`() {
        assertEquals(true, NoticeSearchCondition.normalizeShow("true"))
        assertEquals(false, NoticeSearchCondition.normalizeShow("false"))
        assertEquals(true, NoticeSearchCondition.normalizeShow("TRUE"))
    }

    @Test
    fun `분류는 대소문자를 가리지 않는다`() {
        assertEquals(NoticeType.EVENT, NoticeSearchCondition.normalizeType("event"))
        assertEquals(NoticeType.EVENT, NoticeSearchCondition.normalizeType("EVENT"))
        assertNull(NoticeSearchCondition.normalizeType("없는분류"))
        assertNull(NoticeSearchCondition.normalizeType(""))
    }

    @Test
    fun `검색어의 앞뒤 공백은 제거하고 빈 문자열은 없앤다`() {
        assertEquals("점검", NoticeSearchCondition(keyword = "  점검  ").normalized().keyword)
        assertNull(NoticeSearchCondition(keyword = "   ").normalized().keyword)
    }

    @Test
    fun `알 수 없는 정렬값은 최신순으로 떨어진다`() {
        assertEquals(NoticeSort.LATEST, NoticeSort.from("이상한값"))
        assertEquals(NoticeSort.LATEST, NoticeSort.from(null))
        assertEquals(NoticeSort.OLDEST, NoticeSort.from("oldest"))
    }
}
