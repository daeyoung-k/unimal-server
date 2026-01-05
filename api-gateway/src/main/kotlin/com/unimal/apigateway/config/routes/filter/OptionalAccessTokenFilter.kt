package com.unimal.apigateway.config.routes.filter

import com.unimal.apigateway.service.token.JWTProvider
import com.unimal.apigateway.service.token.TokenService
import com.unimal.common.enums.TokenType
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.text.removePrefix

@Component
@Order(-2)
class OptionalAccessTokenFilter(
    private val jwtProvider: JWTProvider,
    private val tokenService: TokenService,
) : AbstractGatewayFilterFactory<OptionalAccessTokenFilter.Config>(Config::class.java) {
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
            if (!tokenBearer.isNullOrBlank()) {
                // 토큰이 있는 경우에만 정보 전달
                val token = tokenBearer.removePrefix("Bearer ").trim()
                val tokenClaims = jwtProvider.tokenValidation(token)
                val userInfo = tokenService.getUserInfo(tokenClaims, token)
                exchange.request.headers.add(config.addHeaderEmail, userInfo.email)
                exchange.request.headers.add(config.addHeaderNickname, userInfo.nickname)
                exchange.request.headers.add(config.addHeaderRoles, userInfo.roleString)
                exchange.request.headers.add(config.addHeaderProvider, userInfo.provider)
                exchange.request.headers.add(config.addHeaderTokenType, userInfo.tokenType.name)

                // 토큰이 있을경우 엑세스토큰 검사 (엑세스 토큰으로만 기능 접근이 가능)
                if (userInfo.tokenType != TokenType.ACCESS) {
                    val body = """
                    {
                        "code": ${HttpStatus.UNAUTHORIZED.value()},
                        "message": "엑세스 토큰만 허용됩니다.",
                        "data": {}
                    }
                    """.trimIndent().toByteArray(Charsets.UTF_8)
                    val buffer = exchange.response.bufferFactory().wrap(body)
                    return@GatewayFilter exchange.response.writeWith(Mono.just(buffer))
                }
            }
            chain.filter(exchange)
        }
    }
}