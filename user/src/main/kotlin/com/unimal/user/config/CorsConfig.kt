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

    /**
     * Registers the custom argument resolver for handling parameters annotated with `@SocialLoginToken`.
     *
     * Adds `SocialLoginTokenResolver` to the list of argument resolvers, enabling automatic extraction of social login tokens from HTTP requests.
     */
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(SocialLoginTokenResolver())
    }

    inner class SocialLoginTokenResolver : HandlerMethodArgumentResolver {
        /**
         * Determines whether the method parameter is annotated with `@SocialLoginToken`.
         *
         * @return `true` if the parameter has the `@SocialLoginToken` annotation; otherwise, `false`.
         */
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.getParameterAnnotation(SocialLoginToken::class.java) != null
        }

        /**
         * Resolves a method argument annotated with `@SocialLoginToken` by extracting the "Authorization" header from the HTTP request.
         *
         * @return The value of the "Authorization" header.
         * @throws LoginException if the "Authorization" header is missing from the request.
         */
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