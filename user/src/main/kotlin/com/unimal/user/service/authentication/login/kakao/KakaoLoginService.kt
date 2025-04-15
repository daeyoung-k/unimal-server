package com.unimal.user.service.authentication.login.kakao

import com.google.gson.Gson
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.exception.LoginException
import com.unimal.user.service.authentication.login.Login
import com.unimal.user.service.authentication.login.LoginType
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.kakao.dto.KakaoInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component("KakaoLogin")
class KakaoLogin(
    private val memberRepository: MemberRepository
) : Login {
    override fun provider() = LoginType.KAKAO

    fun getInfo(token: String): UserInfo {
        val gson = Gson()
        val url = "https://kapi.kakao.com/v2/user/me"
        val restTemplate = RestTemplate()
        val header = HttpHeaders()
        header.add("Content-Type", "application/json")
        header.add("Authorization", "Bearer $token")

        val entity = HttpEntity<String>(null, header)
        try {
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            val info = gson.fromJson(response.body, KakaoInfo::class.java)
            return UserInfo(
                provider = LoginType.KAKAO.name,
                email = info.kakao_account.email,
                nickname = info.kakao_account.profile.nickname
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw LoginException("kakao login error")
        }
    }

    fun getMember(email: String) = memberRepository.findByEmailAndProvider(email, LoginType.KAKAO.name)

    fun signIn(userInfo: UserInfo) = memberRepository.save(userInfo.toEntity())
}