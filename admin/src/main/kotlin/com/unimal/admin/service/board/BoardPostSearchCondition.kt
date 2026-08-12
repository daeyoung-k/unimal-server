package com.unimal.admin.service.board

import org.springframework.data.domain.Sort

/**
 * 게시글 목록 검색 조건.
 *
 * 회원 관리([com.unimal.admin.service.appmember.AppMemberSearchCondition])와 같은 모양으로
 * 맞춘다 — 어드민 화면이 늘어날 때마다 필터 다루는 방식이 제각각이면 화면을 옮겨 다닐
 * 때마다 코드를 다시 읽어야 한다.
 */
data class BoardPostSearchCondition(
    val show: BoardPostShow? = null,
    /** `null` 이면 삭제 포함 전체. 삭제된 글도 찾아야 문의에 답할 수 있다. */
    val del: Boolean? = null,
    /** `null` 전체 / `true` 사진 게시글 / `false` 텍스트 게시글 (board_file 존재 여부). */
    val hasImage: Boolean? = null,
    val keyword: String? = null,
    val sort: BoardPostSort = BoardPostSort.LATEST,
) {
    fun normalized(): BoardPostSearchCondition = copy(
        keyword = keyword?.trim()?.takeIf { it.isNotEmpty() },
    )

    companion object {
        fun normalizeShow(show: String?): BoardPostShow? {
            val normalizedShow = show?.trim()?.takeIf { it.isNotEmpty() }

            return BoardPostShow.entries.firstOrNull {
                it.name.equals(normalizedShow, ignoreCase = true)
            }
        }

        /**
         * 화면의 true/false/전체 3상 선택값을 해석한다 (삭제 상태·사진 유무 공용).
         *
         * 빈 문자열("전체")과 `"true"`/`"false"` 를 구분해야 하므로 `toBoolean()` 을 쓰지
         * 않는다. `"".toBoolean()` 은 `false` 라서 "전체" 가 다른 의미로 바뀐다.
         */
        fun normalizeFlag(value: String?): Boolean? {
            return when (value?.trim()?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }
    }
}

/**
 * `board` 모듈 `PostShow` 의 어드민용 거울.
 *
 * 원본 enum 은 board 모듈 안에 있어 admin 이 직접 못 쓴다(admin 은 common 만 의존).
 * DB 에는 문자열로 저장되므로 이름만 맞으면 되고, description 은 화면 표기용이다.
 * **board 모듈에 값이 추가되면 여기에도 같이 넣어야 한다** — 없으면 필터에 안 잡히고
 * 화면에 원문 코드가 그대로 노출된다.
 */
enum class BoardPostShow(
    val description: String,
) {
    PUBLIC("전체 공개"),
    PRIVATE("감춤"),
    FRIENDS("친구만 공개"),
    BLOCKED("관리자 블락");

    companion object {
        /** 화면 배지 표기. 목록에 미지의 값이 와도 죽지 않게 원문으로 폴백한다. */
        fun describe(value: String): String =
            entries.firstOrNull { it.name == value }?.description ?: value
    }
}

enum class BoardPostSort(
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
        fun from(value: String?): BoardPostSort {
            val normalizedValue = value?.trim()

            return entries.firstOrNull {
                it.parameterName.equals(normalizedValue, ignoreCase = true) ||
                    it.name.equals(normalizedValue, ignoreCase = true)
            } ?: LATEST
        }
    }
}
