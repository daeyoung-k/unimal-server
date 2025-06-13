package com.unimal.server.webcommon.exception

enum class ErrorCode(
    val code: Int,
    val message: String
) {
    DEFAULT_ERROR(9999, "알 수 없는 에러입니다."),
    VALIDATION_ERROR(9998, "유효성 검사에 실패했습니다."),

    LOGIN_NOT_SUPPORTED(1001, "지원하지 않는 로그인입니다."),
    INVALID_TOKEN(1002, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(1003, "토큰이 없습니다."),
    EXPIRED_TOKEN(1004, "만료된 토큰입니다."),
    SIGNATURE_TOKEN(1005, "잘못된 서명입니다."),

    USER_NOT_FOUND(2001, "사용자 정보를 찾을 수 없습니다."),
    ROLE_NOT_FOUND(2002, "존재하지 않는 권한입니다."),
    EMAIL_NOT_FOUND(2003, "존재하지 않는 이메일입니다."),
    PROVIDER_NOT_FOUND(2004, "존재하지 않는 소셜종류입니다."),

    API_CALL_ERROR(3001, "API 호출에 실패했습니다."),
}