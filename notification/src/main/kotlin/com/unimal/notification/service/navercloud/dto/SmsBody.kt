package com.unimal.notification.service.navercloud.dto

/**
 * type
 * - SMS, LMS, MMS (소문자 가능)
 * contentType
 * - COMM: 일반메시지
 * - AD: 광고메시지
 * - default: COMM
 * countryCode
 * - SENS에서 제공하는 국가로의 발송만 가능
 * - default: 82
 * - 국가코드 안내: https://guide.ncloud-docs.com/docs/sens-smspolicy
 */

data class SmsBody(
    val type: String = "SMS",
    val contentType: String = "COMM",
    val countryCode: String = "82",
    val from: String,
    val content: String,
    val messages: List<MessageTo>
) {
    constructor(
        from: String,
        content: String,
        toList: List<String>
    ) : this(
        from = from,
        content = content,
        messages = toList.map { MessageTo(to = it) }
    )
}

data class MessageTo(
    val to: String,
)