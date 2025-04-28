package com.unimal.user.service.authentication.login.kakao

import com.google.gson.Gson
import com.unimal.user.domain.member.Member
import com.unimal.user.domain.member.MemberRepository
import com.unimal.user.domain.role.MemberRoleRepository
import com.unimal.user.domain.role.RoleRepository
import com.unimal.user.domain.role.enums.MemberRoleCode
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import com.unimal.user.service.authentication.login.Login
import com.unimal.user.service.authentication.login.enums.LoginType
import com.unimal.user.service.authentication.login.dto.UserInfo
import com.unimal.user.service.authentication.login.kakao.dto.KakaoInfo
import com.unimal.user.service.authentication.token.JwtProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component("KakaoLogin")
class KakaoLogin(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val roleRepository: RoleRepository,
    private val jwtProvider: JwtProvider
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
            throw LoginException(ErrorCode.USER_NOT_FOUND.message)
        }
    }

    fun getMember(email: String) = memberRepository.findByEmailAndProvider(email, LoginType.KAKAO.name)

    fun signIn(userInfo: UserInfo): Member {
        val member = memberRepository.save(userInfo.toEntity())
        val role = roleRepository.findByName(MemberRoleCode.USER.name)
            ?: throw LoginException(ErrorCode.ROLE_NOT_FOUND.message)
        println("회원 가입 됐습니다.")
        memberRoleRepository.save(
            member.getMemberRole(role)
        )
        return member
    }

    fun createAccessJwtToken(
        email: String,
        role: List<String>
    ): String {
        return jwtProvider.createAccessToken(
            email = email,
            provider = LoginType.KAKAO,
            roles = role
        )
    }

    fun createRefreshJwtToken(
        email: String,
        role: List<String>
    ): String {
        return jwtProvider.createRefreshToken(
            email = email,
            provider = LoginType.KAKAO,
            roles = role
        )
    }
}