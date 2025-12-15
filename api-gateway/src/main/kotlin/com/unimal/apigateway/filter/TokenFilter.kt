package com.unimal.apigateway.filter

import com.unimal.apigateway.service.token.JWTProvider
import com.unimal.apigateway.service.token.TokenService
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Order(-2)
class TokenFilter(
    private val jwtProvider: JWTProvider,
    private val tokenService: TokenService,
) : AbstractGatewayFilterFactory<TokenFilter.Config>(Config::class.java) {
    class Config {
        val tokenHeaderName = "Authorization"
        val addHeaderEmail = "X-Unimal-User-email"
        val addHeaderNickname = "X-Unimal-User-nickname"
        val addHeaderRoles = "X-Unimal-User-roles"
        val addHeaderProvider = "X-Unimal-User-provider"
        val addHeaderTokenType = "X-Unimal-User-token-type"

    }
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val tokenBearer = exchange.request.headers.getFirst(config.tokenHeaderName)
            if (tokenBearer.isNullOrBlank()) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.add("Content-Type", "application/json")
                val body = """
                    {
                        "code": ${HttpStatus.UNAUTHORIZED.value()},
                        "message": "token is missing"
                    }
                    """.trimIndent().toByteArray(Charsets.UTF_8)
                val buffer = exchange.response.bufferFactory().wrap(body)
                return@GatewayFilter exchange.response.writeWith(Mono.just(buffer))
            }
            // 토큰 체크 -> 유저정보 전달, 토큰 오류시 예외발생.
            val token = tokenBearer.removePrefix("Bearer ").trim()
            val tokenClaims = jwtProvider.tokenValidation(token)
            val userInfo = tokenService.getUserInfo(tokenClaims, token)
            exchange.request.headers.add(config.addHeaderEmail, userInfo.email)
            exchange.request.headers.add(config.addHeaderNickname, userInfo.nickname)
            exchange.request.headers.add(config.addHeaderRoles, userInfo.roleString)
            exchange.request.headers.add(config.addHeaderProvider, userInfo.provider)
            exchange.request.headers.add(config.addHeaderTokenType, userInfo.tokenType.name)
            chain.filter(exchange)
        }
    }
}