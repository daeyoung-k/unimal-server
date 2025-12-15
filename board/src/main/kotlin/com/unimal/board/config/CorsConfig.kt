package com.unimal.board.config

import com.unimal.common.annotation.user.OptionalUserInfoAnnotation
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.TokenException
import com.unimal.webcommon.exception.UserNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig: WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PATCH", "OPTIONS", "DELETE")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(UserInfoResolver())
        resolvers.add(OptionalUserInfoResolver())
    }

    inner class UserInfoResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.getParameterAnnotation(UserInfoAnnotation::class.java) != null
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any? {
            val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            val userInfo = CommonUserInfo(
                email = request?.getHeader("X-Unimal-User-email") ?: throw UserNotFoundException(ErrorCode.EMAIL_NOT_FOUND.message),
                nickname = request.getHeader("X-Unimal-User-nickname") ?: throw UserNotFoundException(ErrorCode.NICKNAME_NOT_FOUND.message),
                roles = request.getHeader("X-Unimal-User-roles")?.split(",") ?: throw UserNotFoundException(ErrorCode.ROLE_NOT_FOUND.message),
                provider = request.getHeader("X-Unimal-User-provider") ?: throw UserNotFoundException(ErrorCode.PROVIDER_NOT_FOUND.message),
                tokenType = TokenType.from(request.getHeader("X-Unimal-User-token-type")) ?: throw TokenException(ErrorCode.TOKEN_TYPE_NOT_FOUND.message, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED)
            )

            // 액세스 토큰만 허용
            return tokenTypeCheck(userInfo)

        }
    }

    inner class OptionalUserInfoResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.getParameterAnnotation(OptionalUserInfoAnnotation::class.java) != null
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any? {
            val request = webRequest.getNativeRequest(HttpServletRequest::class.java)

            val email = request?.getHeader("X-Unimal-User-email")
            val nickname = request?.getHeader("X-Unimal-User-nickname")
            val roles = request?.getHeader("X-Unimal-User-roles")
            val provider = request?.getHeader("X-Unimal-User-provider")
            val tokenType = TokenType.from(request?.getHeader("X-Unimal-User-token-type"))

            if (email.isNullOrBlank() || nickname.isNullOrBlank() || roles.isNullOrBlank() || provider.isNullOrBlank() || tokenType == null) return null

            val userInfo = CommonUserInfo(
                email = email,
                nickname = nickname,
                roles = roles.split(","),
                provider = provider,
                tokenType = tokenType
            )

            // 액세스 토큰만 허용
            return tokenTypeCheck(userInfo)
        }
    }

    private fun tokenTypeCheck(
        commonUserInfo: CommonUserInfo
    ): CommonUserInfo {
        return if (commonUserInfo.tokenType == TokenType.ACCESS) {
            commonUserInfo
        } else {
            throw TokenException(ErrorCode.ALLOW_ACCESS_TOKEN_ONLY.message, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED)
        }
    }


}