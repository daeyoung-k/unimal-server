package com.unimal.admin.domain.adminmember.enums

enum class AdminMemberStatus(
    private val description: String
) {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    LOCKED("잠금처리"),
    RESIGN("퇴사")
}
