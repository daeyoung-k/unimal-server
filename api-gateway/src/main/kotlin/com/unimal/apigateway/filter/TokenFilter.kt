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
        // Add any configuration properties you need here
    }
    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val refreshToken = exchange.request.headers.getFirst("Authorization")
            if (refreshToken.isNullOrBlank()) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.add("Content-Type", "application/json")
                val body = """
                    {
                        "code": ${HttpStatus.UNAUTHORIZED.value()},
                        "message": "Refresh token is missing"
                    }
                    """.trimIndent().toByteArray(Charsets.UTF_8)
                val buffer = exchange.response.bufferFactory().wrap(body)
                return@GatewayFilter exchange.response.writeWith(Mono.just(buffer))
            }
            // 토큰 체크 -> 유저정보 전달, 토큰 오류시 예외발생.
            val tokenClaims = jwtProvider.tokenValidation(refreshToken.removePrefix("Bearer ").trim())
            val userInfo = tokenService.getUserInfo(tokenClaims)
            exchange.response.headers.add("X-Unimal-User-email", userInfo.email)
            exchange.response.headers.add("X-Unimal-User-roles", userInfo.roles.toString())
            exchange.response.headers.add("X-Unimal-User-provider", userInfo.provider)
            chain.filter(exchange)
        }
    }
}