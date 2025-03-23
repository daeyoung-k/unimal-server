package com.unimal.user.service.login.kakao

import com.google.gson.Gson
import com.unimal.user.service.login.Login
import com.unimal.user.service.login.LoginType
import com.unimal.user.service.login.kakao.dto.KakaoInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KakaoLogin : Login {
    override fun provider() = LoginType.KAKAO

    fun getInfo(token: String) {
        val gson = Gson()
        val url = "https://kapi.kakao.com/v2/user/me"
        val restTemplate = RestTemplate()
        val header = HttpHeaders()
        header.add("Content-Type", "application/json")
        header.add("Authorization", "Bearer $token")

        val entity = HttpEntity<String>(null, header)
        try {
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            val kakaoInfo = gson.fromJson(response.body, KakaoInfo::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }



    }
}