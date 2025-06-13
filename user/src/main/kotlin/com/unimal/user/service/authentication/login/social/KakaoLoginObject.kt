package com.unimal.user.service.authentication.login.social

import com.google.gson.Gson
import com.unimal.server.webcommon.exception.ApiCallException
import com.unimal.server.webcommon.exception.ErrorCode
import com.unimal.server.webcommon.exception.LoginException
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.authentication.login.dto.KakaoInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component("KakaoLoginObject")
class KakaoLoginObject: LoginInterface {
    override fun provider() = LoginType.KAKAO
    override fun <T> getInfo(info: T): UserInfo {
        val gson = Gson()
        val url = "https://kapi.kakao.com/v2/user/me"
        val restTemplate = RestTemplate()
        val header = HttpHeaders()
        header.add("Content-Type", "application/json")
        header.add("Authorization", "Bearer $info")

        val entity = HttpEntity<String>(null, header)
        try {
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            val kakaoInfo = gson.fromJson(response.body, KakaoInfo::class.java)
            return UserInfo(
                provider = LoginType.KAKAO.name,
                email = kakaoInfo.kakao_account.email,
                nickname = kakaoInfo.kakao_account.profile.nickname
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw ApiCallException("카카오 유저 정보 조회 - ${ErrorCode.API_CALL_ERROR.message}")
        }
    }
}