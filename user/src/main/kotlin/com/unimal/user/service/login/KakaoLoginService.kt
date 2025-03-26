package com.unimal.user.service.login

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KakaoLogin : Login {
    override fun provider() = LoginType.KAKAO

    fun getInfo(token: String) {
        val url = "https://kapi.kakao.com/v2/user/me"
        val restTemplate = RestTemplate()
        val header = HttpHeaders()
        header.add("Content-Type", "application/json")
        header.add("Authorization", "Bearer $token")

        val entity = HttpEntity<String>(null, header)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
        println(response)

    }
}