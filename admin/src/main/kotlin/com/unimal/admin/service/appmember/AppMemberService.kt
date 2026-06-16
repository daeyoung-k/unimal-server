package com.unimal.admin.service.appmember

import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.domain.appmember.AppMemberRepository
import com.unimal.common.enums.UserStatus
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppMemberService(
    private val appMemberRepository: AppMemberRepository
) {

    @Transactional(readOnly = true)
    fun getMembers(
        page: Int,
        size: Int,
        condition: AppMemberSearchCondition = AppMemberSearchCondition()
    ): Page<AppMember> {
        val normalizedCondition = condition.normalized()
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            normalizedCondition.sort.toSort()
        )

        return appMemberRepository.findAll(normalizedCondition.toSpecification(), pageable)
    }

    @Transactional(readOnly = true)
    fun getProviderCounts(
        condition: AppMemberSearchCondition = AppMemberSearchCondition()
    ): List<AppMemberProviderCount> {
        val normalizedCondition = condition.normalized()

        return AppMemberSearchCondition.providerOptions.map { providerOption ->
            val count = if (
                normalizedCondition.provider != null &&
                normalizedCondition.provider != providerOption.value
            ) {
                0L
            } else {
                appMemberRepository.count(
                    normalizedCondition
                        .copy(provider = providerOption.value)
                        .toSpecification()
                )
            }

            AppMemberProviderCount(
                value = providerOption.value,
                label = providerOption.label,
                count = count
            )
        }
    }

    private fun AppMemberSearchCondition.toSpecification(): Specification<AppMember> =
        Specification { root, _, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            status?.let {
                predicates.add(criteriaBuilder.equal(root.get<UserStatus>("status"), it))
            }

            provider?.let {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("provider")), it))
            }

            keyword?.let {
                val pattern = "%${it.lowercase()}%"
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nickname")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern)
                    )
                )
            }

            if (predicates.isEmpty()) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
}
