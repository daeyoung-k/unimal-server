package com.unimal.admin.domain.adminmember.enums

enum class AdminRole(
    private val description: String
) {
    ADMIN("관리자"),
    SUPER_ADMIN("슈퍼 관리자")
}
