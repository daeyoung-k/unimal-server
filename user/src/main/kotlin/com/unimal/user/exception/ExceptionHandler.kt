package com.unimal.user.exception

import com.unimal.common.dto.CommonResponse
import com.unimal.webcommon.exception.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun dataIntegrityViolationException(ex: DataIntegrityViolationException): ResponseEntity<CommonResponse> {
        return ResponseEntity(
            CommonResponse(
                code = HttpStatus.CONFLICT.value(),
                message = if (!ex.message.isNullOrBlank()) ex.message else ErrorCode.DUPLICATE_KEY.message
            ),  HttpStatus.CONFLICT)
    }
}