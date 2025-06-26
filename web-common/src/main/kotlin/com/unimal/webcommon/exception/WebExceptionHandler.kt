package com.unimal.webcommon.exception

import com.unimal.common.dto.CommonResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.nio.file.attribute.UserPrincipalNotFoundException
import java.security.SignatureException

@RestControllerAdvice
class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handlerValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<CommonResponse> {
        val errors = mutableMapOf<String, String?>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName: String = if (error is FieldError) {
                error.field
            } else {
                "Unknown Field"
            }
            val errorMessage = error.defaultMessage ?: ErrorCode.VALIDATION_ERROR.message

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
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.DEFAULT_ERROR.message
            ),  ex.status ?: HttpStatus.OK)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun expiredJwtException(ex: ExpiredJwtException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.EXPIRED_TOKEN.message
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(SignatureException::class)
    fun signatureException(ex: SignatureException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.SIGNATURE_TOKEN.message
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun malformedJwtException(ex: MalformedJwtException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.INVALID_TOKEN.message
            ),  HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(UserPrincipalNotFoundException::class)
    fun userPrincipalNotFoundException(ex: UserPrincipalNotFoundException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.UNAUTHORIZED.value(),
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.USER_NOT_FOUND.message
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

class ApiCallException(
    message: String?,
    code: Int? = null,
    status: HttpStatus? = null
) : CustomException(message, code, status)