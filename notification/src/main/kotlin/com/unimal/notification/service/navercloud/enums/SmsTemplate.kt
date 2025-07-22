package com.unimal.notification.service.navercloud.enums

enum class SmsTemplate(
    val description: String,
    val template: (String) -> String
) {

    AUTH_CODE(
        "전화번호 인증 템플릿",
        { code ->
            "[유니멀] 인증번호 [$code] 타인에게는 알려주지 마세요."
        }
    );
}