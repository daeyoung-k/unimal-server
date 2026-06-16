package com.unimal.admin.domain.appmember.actionlog

enum class AppMemberActionType(
    val description: String,
) {
    PROFILE_IMAGE_RESET("프로필 이미지 초기화"),
    INTRODUCTION_HIDE("소개글 숨김"),
    MEMBER_BLOCK("회원 차단"),
    MEMBER_UNBLOCK("회원 차단 해제"),
}
