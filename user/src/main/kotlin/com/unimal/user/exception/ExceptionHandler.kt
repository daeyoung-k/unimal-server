package com.unimal.user.exception

import com.unimal.common.dto.CommonResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.nio.file.attribute.UserPrincipalNotFoundException

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handlerValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<CommonResponse> {
        val errors = mutableMapOf<String, String?>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName: String = if (error is FieldError) {
                error.field
            } else {
                "Unknown Field"
            }
            val errorMessage = error.defaultMessage ?: "Validation error"

            errors["message"] = "$fieldName: $errorMessage"
        }
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.BAD_REQUEST.value(),
                message = errors["message"]
            ),  HttpStatus.OK)
    }

    @ExceptionHandler(CustomException::class)
    fun handlerException(ex: CustomException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = ex.code ?: HttpStatus.BAD_REQUEST.value(),
                message = if (!ex.message.isNullOrBlank()) "module:user - ${ex.message}" else "module:user - error"
            ),  ex.status ?: HttpStatus.OK)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun expiredJwtException(ex: ExpiredJwtException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) "module:user - ${ex.message}" else "module:user - 토큰이 만료되었습니다."
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(SignatureException::class)
    fun signatureException(ex: SignatureException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) "module:user - ${ex.message}" else "module:user - 잘못된 서명입니다."
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun malformedJwtException(ex: MalformedJwtException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) "module:user - ${ex.message}" else "module:user - 잘못된 토큰입니다."
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(UserPrincipalNotFoundException::class)
    fun userPrincipalNotFoundException(ex: UserPrincipalNotFoundException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) "module:user - ${ex.message}" else "module:user - 사용자 정보를 찾을 수 없습니다."
            ),  HttpStatus.UNAUTHORIZED)
    }
}

open class CustomException(
    message: String?,
    val code: Int? = null,
    val status: HttpStatus? = null
) : Exception(message)

class LoginException(
    message: String?,
    code: Int? = null,
    status: HttpStatus? = null
) : CustomException(message, code, status)

class TokenException(
    message: String?,
    code: Int? = null,
    status: HttpStatus? = null
) : CustomException(message, code, status)

class UserNotFoundException(
    message: String?,
    code: Int? = null,
    status: HttpStatus? = null
) : CustomException(message, code, status)