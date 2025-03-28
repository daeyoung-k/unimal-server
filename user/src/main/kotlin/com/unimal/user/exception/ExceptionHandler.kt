package com.unimal.user.exception

import com.unimal.common.dto.CommonResponse
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
}

open class CustomException(message: String?, val code: Int? = null) : Exception(message)

class LoginException(message: String?) : CustomException(message)