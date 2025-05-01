package com.unimal.apigateway.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
@Order(-2)
class GlobalExceptionHandler: ErrorWebExceptionHandler {
    private val logger = KotlinLogging.logger {}

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        var code: Int = HttpStatus.BAD_REQUEST.value()
        val response = exchange.response
        response.statusCode = HttpStatus.BAD_REQUEST
        response.headers.contentType = MediaType.APPLICATION_JSON
        logger.info { "발생한 예외: $ex" }
        val message = when (ex) {
            is CustomException -> {
                code = HttpStatus.BAD_REQUEST.value()
                response.statusCode = HttpStatus.BAD_REQUEST
                ex.message ?: "error"
            }
            is TokenNotFoundException -> {
                code = HttpStatus.UNAUTHORIZED.value()
                response.statusCode = HttpStatus.UNAUTHORIZED
                ex.message ?: "토큰이 만료 되었습니다."
            }
            is SignatureException -> {
                code = HttpStatus.UNAUTHORIZED.value()
                response.statusCode = HttpStatus.UNAUTHORIZED
                "잘못된 서명입니다."
            }

            is ExpiredJwtException -> {
                code = HttpStatus.UNAUTHORIZED.value()
                response.statusCode = HttpStatus.UNAUTHORIZED
                "토큰이 만료되었습니다."
            }
            is MalformedJwtException -> {
                code = HttpStatus.UNAUTHORIZED.value()
                response.statusCode = HttpStatus.UNAUTHORIZED
                "잘못된 토큰입니다."
            }
            else -> ex.message ?: "알 수 없는 오류입니다."
        }

        val body = """
            {
                "code": $code,
                "message": "$message"
            }
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        val buffer = response.bufferFactory().wrap(body)
        return response.writeWith(Mono.just(buffer))
    }
}