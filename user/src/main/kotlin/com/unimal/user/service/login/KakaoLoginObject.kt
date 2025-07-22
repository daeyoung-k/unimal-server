package com.unimal.user.service.login

import com.google.gson.Gson
import com.unimal.user.domain.member.Member
import com.unimal.webcommon.exception.ApiCallException
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.user.service.login.dto.UserInfo
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.login.dto.KakaoInfo
import com.unimal.user.service.member.MemberObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component("KakaoLoginObject")
class KakaoLoginObject(
    private val memberObject: MemberObject
): LoginInterface {
    override fun provider() = LoginType.KAKAO
    override fun <T> getUserInfo(info: T): UserInfo {
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
                provider = provider().name,
                email = kakaoInfo.kakao_account.email,
                nickname = kakaoInfo.kakao_account.profile.nickname
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw ApiCallException("카카오 유저 정보 조회 - ${ErrorCode.API_CALL_ERROR.message}")
        }
    }

    override fun getMember(userInfo: UserInfo): Member {
        return memberObject.getEmailMember(userInfo.email) ?: memberObject.signIn(userInfo)
    }
}