package com.unimal.user.service.token

import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.service.login.enums.LoginType
import com.unimal.user.service.token.dto.JwtTokenDTO
import com.unimal.user.service.member.MemberObject
import com.unimal.webcommon.exception.CustomException
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jwtProvider: JwtProvider,
    private val tokenManager: TokenManager,
    private val memberObject: MemberObject,
    ) {

    @Transactional
    fun accessTokenCreate(commonUserInfo: CommonUserInfo): JwtTokenDTO {

        if (commonUserInfo.tokenType != "refresh") {
            throw CustomException(
                message = "잘못된 토큰 타입입니다.",
                code = HttpStatus.UNAUTHORIZED.value(),
                status = HttpStatus.UNAUTHORIZED
            )
        }

        val member = memberObject.getMember(
            email = commonUserInfo.email,
            provider = LoginType.from(commonUserInfo.provider)
        ) ?: throw UserNotFoundException(
            message = "회원이 존재하지 않습니다.",
            code = HttpStatus.UNAUTHORIZED.value(),
            status = HttpStatus.UNAUTHORIZED
        )

        val roles = member.roles.map { it.roleName.name }

        val provider = LoginType.from(member.provider)

        val accessToken = jwtProvider.createAccessToken(
            email = member.email,
            provider = provider,
            roles = roles
        )
        tokenManager.saveCacheToken(member.email, accessToken)

        val refreshToken = jwtProvider.createRefreshToken(
            email = member.email,
            provider = provider,
            roles = roles
        )
        tokenManager.upsertDbToken(member.email, refreshToken)

        return JwtTokenDTO(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}