package com.unimal.apigateway.filter

import com.unimal.apigateway.service.JWTProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Order(-2)
class RefreshTokenFilter(
    private val jwtProvider: JWTProvider
) : AbstractGatewayFilterFactory<RefreshTokenFilter.Config>(Config::class.java) {
    class Config {
        // Add any configuration properties you need here
    }
    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val refreshToken = exchange.request.headers.getFirst("X-Unimal-Refresh-Token")
            if (refreshToken.isNullOrBlank()) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.add("Content-Type", "application/json")
                val body = """{"code":401,"message":"Refresh token is missing"}""".toByteArray(Charsets.UTF_8)
                val buffer = exchange.response.bufferFactory().wrap(body)
                return@GatewayFilter exchange.response.writeWith(Mono.just(buffer))
            }
            // 토큰 체크, 토큰 오류시 예외발생.
            jwtProvider.tokenValidation(refreshToken)
            chain.filter(exchange)
        }
    }
}