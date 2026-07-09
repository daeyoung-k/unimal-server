package com.unimal.board.enums

enum class PostShow(
    val description: String
) {
    PUBLIC("전체 공개"),
    PRIVATE("감춤"),         // 작성자 비공개
    FRIENDS("친구만 공개"),  // 추후 팔로잉 기능에서 활용
    BLOCKED("관리자 블락"),  // 관리자 신고 처리 (admin 작업에서 세팅)
}
