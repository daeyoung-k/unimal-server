package com.unimal.user.exception

import com.unimal.common.dto.CommonResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handlerException(ex: CustomException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = ex.code ?: HttpStatus.BAD_REQUEST.value(),
                message = ex.message ?: "error"
            ),  HttpStatus.OK)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun expiredJwtException(ex: ExpiredJwtException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = ex.message ?: "기간 만료 토큰"
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(SignatureException::class)
    fun signatureException(ex: SignatureException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = ex.message ?: "인증 불가 토큰"
            ),  HttpStatus.UNAUTHORIZED)
    }
}

open class CustomException(
    message: String?,
    val code: Int? = null,
) : Exception(message)

class LoginException(message: String?) : CustomException(message)