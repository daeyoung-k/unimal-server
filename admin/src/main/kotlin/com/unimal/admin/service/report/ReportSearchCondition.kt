package com.unimal.admin.service.report

import com.unimal.common.enums.report.ReportReason
import com.unimal.common.enums.report.ReportStatus
import com.unimal.common.enums.report.ReportTargetType
import org.springframework.data.domain.Sort

/**
 * 신고 목록 검색 조건.
 *
 * 기본값이 [ReportStatus.PENDING] 인 것이 다른 화면과 다르다. 신고 관리에서 할 일은
 * "아직 처리 안 한 것 처리하기" 이고, 처리 끝난 신고까지 섞어 보여주면 매번 필터를
 * 다시 걸어야 한다.
 */
data class ReportSearchCondition(
    val status: ReportStatus? = ReportStatus.PENDING,
    val targetType: ReportTargetType? = null,
    val reason: ReportReason? = null,
    val sort: ReportSort = ReportSort.OLDEST,
) {
    companion object {
        /**
         * 상태 파라미터 해석.
         *
         * `null`(파라미터 없음)과 `""`("전체" 선택)를 구분해야 한다.
         * 전자는 첫 진입이라 기본값(미처리)을 쓰고, 후자는 사용자가 명시적으로
         * 전체를 고른 것이라 필터를 걸지 않는다.
         */
        fun normalizeStatus(status: String?): ReportStatus? {
            if (status == null) return ReportStatus.PENDING
            if (status.isBlank()) return null

            return ReportStatus.entries.firstOrNull { it.name.equals(status.trim(), ignoreCase = true) }
        }

        fun normalizeTargetType(targetType: String?): ReportTargetType? {
            val value = targetType?.trim()?.takeIf { it.isNotEmpty() } ?: return null

            return ReportTargetType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }

        fun normalizeReason(reason: String?): ReportReason? {
            val value = reason?.trim()?.takeIf { it.isNotEmpty() } ?: return null

            return ReportReason.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class ReportSort(
    val parameterName: String,
    val label: String,
    private val direction: Sort.Direction,
) {
    /**
     * 오래된 순이 기본이다. 신고는 처리 대기열이라 먼저 들어온 것부터 봐야 한다 —
     * 최신순으로 두면 오래된 신고가 목록 뒤로 밀려 영영 남는다.
     */
    OLDEST(parameterName = "oldest", label = "오래된순", direction = Sort.Direction.ASC),
    LATEST(parameterName = "latest", label = "최신순", direction = Sort.Direction.DESC);

    fun toSort(): Sort = Sort.by(direction, "id")

    companion object {
        fun from(value: String?): ReportSort {
            val normalizedValue = value?.trim()

            return entries.firstOrNull {
                it.parameterName.equals(normalizedValue, ignoreCase = true) ||
                    it.name.equals(normalizedValue, ignoreCase = true)
            } ?: OLDEST
        }
    }
}
