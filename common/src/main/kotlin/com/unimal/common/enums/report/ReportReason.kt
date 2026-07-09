package com.unimal.common.enums.report

enum class ReportReason(
    val description: String
) {
    SPAM("스팸/광고"),
    ABUSE("욕설/비방/혐오"),
    SEXUAL("음란물/선정성"),
    FALSE_INFO("허위정보"),
    ILLEGAL("불법정보"),
    PRIVACY("개인정보 노출"),
    ETC("기타"),
}
