package com.unimal.admin.service.appmember

import com.unimal.admin.domain.adminmember.AdminMember
import com.unimal.admin.domain.adminmember.AdminMemberRepository
import com.unimal.admin.domain.appmember.AppMember
import com.unimal.admin.domain.appmember.AppMemberRepository
import com.unimal.admin.domain.appmember.actionlog.AppMemberActionLog
import com.unimal.admin.domain.appmember.actionlog.AppMemberActionLogRepository
import com.unimal.admin.domain.appmember.actionlog.AppMemberActionType
import com.unimal.common.enums.UserStatus
import jakarta.persistence.criteria.Predicate
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppMemberService(
    private val appMemberRepository: AppMemberRepository,
    private val adminMemberRepository: AdminMemberRepository,
    private val appMemberActionLogRepository: AppMemberActionLogRepository,
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
    fun getMember(memberId: Long): AppMember {
        return appMemberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("App member not found: $memberId") }
    }

    @Transactional(readOnly = true)
    fun getActionLogs(memberId: Long): List<AppMemberActionLog> {
        return appMemberActionLogRepository.findTop20ByTargetMemberIdOrderByCreatedAtDescIdDesc(memberId)
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

    @Transactional
    fun resetProfileImage(memberId: Long, adminLoginId: String, reason: String) {
        val member = getMember(memberId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)
        val updatedCount = appMemberRepository.resetProfileImage(memberId, LocalDateTime.now())

        require(updatedCount == 1) { "Failed to reset profile image: $memberId" }
        recordActionLog(
            adminMember = adminMember,
            targetMember = member,
            actionType = AppMemberActionType.PROFILE_IMAGE_RESET,
            reason = normalizedReason,
            beforeValue = member.profileImage,
            afterValue = null
        )
    }

    @Transactional
    fun hideIntroduction(memberId: Long, adminLoginId: String, reason: String) {
        val member = getMember(memberId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)
        val updatedCount = appMemberRepository.hideIntroduction(memberId, LocalDateTime.now())

        require(updatedCount == 1) { "Failed to hide introduction: $memberId" }
        recordActionLog(
            adminMember = adminMember,
            targetMember = member,
            actionType = AppMemberActionType.INTRODUCTION_HIDE,
            reason = normalizedReason,
            beforeValue = member.introduction,
            afterValue = null
        )
    }

    @Transactional
    fun blockMember(memberId: Long, adminLoginId: String, reason: String) {
        val member = getMember(memberId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)

        require(member.status != UserStatus.WITHDRAWAL) { "Withdrawn member cannot be blocked: $memberId" }
        require(member.status != UserStatus.BLOCK) { "Member is already blocked: $memberId" }

        val updatedCount = appMemberRepository.updateStatus(
            id = memberId,
            status = UserStatus.BLOCK.name,
            updatedAt = LocalDateTime.now()
        )

        require(updatedCount == 1) { "Failed to block member: $memberId" }
        recordActionLog(
            adminMember = adminMember,
            targetMember = member,
            actionType = AppMemberActionType.MEMBER_BLOCK,
            reason = normalizedReason,
            beforeValue = member.status.name,
            afterValue = UserStatus.BLOCK.name
        )
    }

    @Transactional
    fun unblockMember(memberId: Long, adminLoginId: String, reason: String) {
        val member = getMember(memberId)
        val adminMember = getAdminMember(adminLoginId)
        val normalizedReason = normalizeReason(reason)

        require(member.status == UserStatus.BLOCK) { "Only blocked member can be unblocked: $memberId" }

        val updatedCount = appMemberRepository.updateStatus(
            id = memberId,
            status = UserStatus.ACTIVE.name,
            updatedAt = LocalDateTime.now()
        )

        require(updatedCount == 1) { "Failed to unblock member: $memberId" }
        recordActionLog(
            adminMember = adminMember,
            targetMember = member,
            actionType = AppMemberActionType.MEMBER_UNBLOCK,
            reason = normalizedReason,
            beforeValue = member.status.name,
            afterValue = UserStatus.ACTIVE.name
        )
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

    private fun getAdminMember(adminLoginId: String): AdminMember {
        return adminMemberRepository.findByLoginId(adminLoginId)
            ?: throw NoSuchElementException("Admin member not found: $adminLoginId")
    }

    private fun normalizeReason(reason: String): String {
        return reason.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Action reason is required")
    }

    private fun recordActionLog(
        adminMember: AdminMember,
        targetMember: AppMember,
        actionType: AppMemberActionType,
        reason: String,
        beforeValue: String?,
        afterValue: String?,
    ) {
        appMemberActionLogRepository.save(
            AppMemberActionLog(
                adminMemberId = requireNotNull(adminMember.id),
                adminLoginId = adminMember.loginId,
                targetMemberId = requireNotNull(targetMember.id),
                targetMemberEmail = targetMember.email,
                actionType = actionType,
                reason = reason,
                beforeValue = beforeValue,
                afterValue = afterValue
            )
        )
    }
}
