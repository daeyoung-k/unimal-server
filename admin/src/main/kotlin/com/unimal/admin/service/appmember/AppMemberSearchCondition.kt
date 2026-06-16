package com.unimal.admin.service.appmember

import com.unimal.common.enums.UserStatus
import java.util.Locale
import org.springframework.data.domain.Sort

data class AppMemberSearchCondition(
    val status: UserStatus? = null,
    val provider: String? = null,
    val keyword: String? = null,
    val sort: AppMemberSort = AppMemberSort.LATEST,
) {
    fun normalized(): AppMemberSearchCondition = copy(
        provider = normalizeProvider(provider),
        keyword = keyword?.trim()?.takeIf { it.isNotEmpty() },
    )

    companion object {
        val providerOptions = listOf(
            AppMemberProviderOption(value = "KAKAO", label = "카카오"),
            AppMemberProviderOption(value = "NAVER", label = "네이버"),
            AppMemberProviderOption(value = "GOOGLE", label = "구글"),
            AppMemberProviderOption(value = "MANUAL", label = "일반")
        )

        private val providerValues = providerOptions.map { it.value }.toSet()

        fun normalizeProvider(provider: String?): String? {
            val normalizedProvider = provider
                ?.trim()
                ?.uppercase(Locale.ROOT)
                ?.takeIf { it.isNotEmpty() }

            return normalizedProvider?.takeIf { it in providerValues }
        }

        fun normalizeStatus(status: String?): UserStatus? {
            val normalizedStatus = status
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            return UserStatus.entries.firstOrNull {
                it.name.equals(normalizedStatus, ignoreCase = true)
            }
        }
    }
}

data class AppMemberProviderOption(
    val value: String,
    val label: String,
)

data class AppMemberProviderCount(
    val value: String,
    val label: String,
    val count: Long,
)

enum class AppMemberSort(
    val parameterName: String,
    val label: String,
    private val direction: Sort.Direction,
    private val property: String,
) {
    LATEST(
        parameterName = "latest",
        label = "최신순",
        direction = Sort.Direction.DESC,
        property = "createdAt"
    ),
    OLDEST(
        parameterName = "oldest",
        label = "오래된순",
        direction = Sort.Direction.ASC,
        property = "createdAt"
    ),
    UPDATED(
        parameterName = "updated",
        label = "최근 수정순",
        direction = Sort.Direction.DESC,
        property = "updatedAt"
    );

    fun toSort(): Sort = Sort.by(direction, property)

    companion object {
        fun from(value: String?): AppMemberSort {
            val normalizedValue = value?.trim()

            return entries.firstOrNull {
                it.parameterName.equals(normalizedValue, ignoreCase = true) ||
                    it.name.equals(normalizedValue, ignoreCase = true)
            } ?: LATEST
        }
    }
}
