package com.unimal.user.service.login.kakao.dto

data class KakaoInfo(
    val kakao_account: KakaoAccount
)

data class KakaoAccount(
    val email: String,
    val profile: KakaoProfile
)

data class KakaoProfile(
    val nickname: String
)


//{
//    "id" : 3956953872,
//    "connected_at" : "2025-03-10T14:50:38Z",
//    "properties" : {
//    "nickname" : "권대영"
//},
//    "kakao_account" : {
//    "profile_nickname_needs_agreement" : false,
//    "profile" : {
//        "nickname" : "권대영",
//        "is_default_nickname" : false
//    },
//    "has_email" : true,
//    "email_needs_agreement" : false,
//    "is_email_valid" : true,
//    "is_email_verified" : true,
//    "email" : "a5678936@kakao.com"
//}
//}
