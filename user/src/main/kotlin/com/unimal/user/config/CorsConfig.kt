package com.unimal.user.config

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonUserInfo
import com.unimal.common.enums.TokenType
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.LoginException
import com.unimal.webcommon.exception.UserNotFoundException
import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.webcommon.exception.TokenException
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
class CorsConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PATCH", "OPTIONS", "DELETE")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(SocialLoginTokenResolver())
        resolvers.add(UserInfoResolver())
    }

    inner class SocialLoginTokenResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.getParameterAnnotation(SocialLoginToken::class.java) != null
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any? {
            val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            val authHeader = request?.getHeader("Authorization") ?: throw LoginException(message = ErrorCode.TOKEN_NOT_FOUND.message, code = HttpStatus.UNAUTHORIZED.value(), status = HttpStatus.UNAUTHORIZED)
            return authHeader.removePrefix("Bearer ").trim()
        }
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
            return CommonUserInfo(
                email = request?.getHeader("X-Unimal-User-email") ?: throw UserNotFoundException(ErrorCode.EMAIL_NOT_FOUND.message),
                nickname = request.getHeader("X-Unimal-User-nickname") ?: throw UserNotFoundException(ErrorCode.NICKNAME_NOT_FOUND.message),
                roles = request.getHeader("X-Unimal-User-roles")?.split(",") ?: throw UserNotFoundException(ErrorCode.ROLE_NOT_FOUND.message),
                provider = request.getHeader("X-Unimal-User-provider") ?: throw UserNotFoundException(ErrorCode.PROVIDER_NOT_FOUND.message),
                tokenType = TokenType.from(request.getHeader("X-Unimal-User-token-type")) ?: throw TokenException(ErrorCode.TOKEN_TYPE_NOT_FOUND.message, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED)
            )
        }
    }
}