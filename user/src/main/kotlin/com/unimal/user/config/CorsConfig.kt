package com.unimal.user.config

import com.unimal.user.config.annotation.SocialLoginToken
import com.unimal.user.exception.ErrorCode
import com.unimal.user.exception.LoginException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(SocialLoginTokenResolver())
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
            val authHeader = request?.getHeader("Authorization") ?: throw LoginException(ErrorCode.TOKEN_NOT_FOUND.message)
            return authHeader
        }
    }
}