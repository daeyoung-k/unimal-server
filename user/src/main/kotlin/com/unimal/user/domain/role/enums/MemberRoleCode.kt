package com.unimal.user.domain.role.enums

enum class MemberRoleCode(
    val description: String
) {
    USER("일반 사용자"),
    ADMIN("관리자"),
    MANAGER("매니저"),
    GUEST("게스트"),
    SUPER_ADMIN("슈퍼 관리자"),
    DEVELOPER("개발자"),
    ;

    companion object {
        fun fromString(role: String): MemberRoleCode {
            return entries.firstOrNull { it.name == role } ?: USER
        }
    }
}