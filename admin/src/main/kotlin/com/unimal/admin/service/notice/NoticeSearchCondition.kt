package com.unimal.admin.service.notice

import com.unimal.common.enums.notice.NoticeType
import org.springframework.data.domain.Sort

/**
 * 공지 목록 검색 조건.
 *
 * 회원 관리([com.unimal.admin.service.appmember.AppMemberSearchCondition])와 같은 모양으로
 * 맞춘다 — 어드민 화면이 늘어날 때마다 필터 다루는 방식이 제각각이면 화면을 옮겨 다닐
 * 때마다 코드를 다시 읽어야 한다.
 */
data class NoticeSearchCondition(
    val type: NoticeType? = null,
    /** `null` 이면 노출/숨김 모두. 숨긴 공지도 찾아야 복구할 수 있다. */
    val show: Boolean? = null,
    val keyword: String? = null,
    val sort: NoticeSort = NoticeSort.LATEST,
) {
    fun normalized(): NoticeSearchCondition = copy(
        keyword = keyword?.trim()?.takeIf { it.isNotEmpty() },
    )

    companion object {
        fun normalizeType(type: String?): NoticeType? {
            val normalizedType = type?.trim()?.takeIf { it.isNotEmpty() }

            return NoticeType.entries.firstOrNull {
                it.name.equals(normalizedType, ignoreCase = true)
            }
        }

        /**
         * 화면의 노출 상태 선택값을 해석한다.
         *
         * 빈 문자열("전체")과 `"true"`/`"false"` 를 구분해야 하므로 `toBoolean()` 을 쓰지
         * 않는다. `"".toBoolean()` 은 `false` 라서 "전체" 가 "숨김만" 으로 바뀐다.
         */
        fun normalizeShow(show: String?): Boolean? {
            return when (show?.trim()?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }
    }
}

enum class NoticeSort(
    val parameterName: String,
    val label: String,
    private val direction: Sort.Direction,
    private val property: String,
) {
    LATEST(
        parameterName = "latest",
        label = "최신순",
        direction = Sort.Direction.DESC,
        property = "id"
    ),
    OLDEST(
        parameterName = "oldest",
        label = "오래된순",
        direction = Sort.Direction.ASC,
        property = "id"
    );

    fun toSort(): Sort = Sort.by(direction, property)

    companion object {
        fun from(value: String?): NoticeSort {
            val normalizedValue = value?.trim()

            return entries.firstOrNull {
                it.parameterName.equals(normalizedValue, ignoreCase = true) ||
                    it.name.equals(normalizedValue, ignoreCase = true)
            } ?: LATEST
        }
    }
}
