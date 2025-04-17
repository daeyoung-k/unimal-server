package com.unimal.user.exception

enum class ErrorCode(
    val code: Int,
    val message: String
) {
    LOGIN_NOT_SUPPORTED(1001, "지원하지 않는 로그인입니다."),
    INVALID_TOKEN(1002, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(1003, "토큰이 없습니다."),
    USER_NOT_FOUND(1004, "존재하지 않는 사용자입니다."),
}