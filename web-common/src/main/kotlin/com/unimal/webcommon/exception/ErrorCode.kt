package com.unimal.webcommon.exception

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
    TOKEN_TYPE_NOT_FOUND(1006, "존재하지 않는 토큰 타입입니다."),
    ALLOW_ACCESS_TOKEN_ONLY(1007, "액세스 토큰만 허용됩니다."),
    AUTH_CODE_NOT_FOUND(1008, "인증코드가 존재하지 않습니다."),
    TEL_NOT_FOUND(1009, "휴대폰 번호가 존재하지 않습니다."),
    AUTH_CODE_NOT_MATCH(1010, "인증코드가 일치하지 않습니다."),
    NICKNAME_USED(1011, "이미 사용중인 닉네임입니다."),

    USER_NOT_FOUND(2001, "사용자 정보를 찾을 수 없습니다."),
    ROLE_NOT_FOUND(2002, "존재하지 않는 권한입니다."),
    EMAIL_NOT_FOUND(2003, "존재하지 않는 이메일입니다."),
    PROVIDER_NOT_FOUND(2004, "존재하지 않는 소셜종류입니다."),
    PASSWORD_NOT_MATCH(2005, "비밀번호가 일치하지 않습니다."),
    EMAIL_USED(2006, "이미 사용중인 이메일입니다."),
    TEL_USED(2007, "이미 사용중인 전화번호입니다."),
    PASSWORD_FORMAT_INVALID(2008, "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."),
    AUTHENTICATION_NOT_COMPLETED(2009, "이메일 인증 또는 휴대폰 인증이 완료되지 않았습니다."),
    EXISTING_PASSWORD_NOT_CHANGE(2010, "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    FILE_UPLOAD_ERROR(2011, "파일 업로드에 실패했습니다."),
    MULTIFILE_UPLOAD_ERROR(2012, "다중 파일 업로드에 실패했습니다."),
    BOARD_NOT_FOUND(2013, "게시판 정보를 찾을 수 없습니다."),
    BOARD_OWNER_NOT_MATCH(2014, "게시판 작성자가 아닙니다."),

    API_CALL_ERROR(3001, "API 호출에 실패했습니다."),
    HASHIDS_ENCODE_ERROR(3002, "Hashids encode 오류입니다."),
    HASHIDS_DECODE_ERROR(3003, "Hashids decode 오류입니다.")
}