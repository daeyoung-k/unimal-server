package com.unimal.apigateway.exception

import org.springframework.http.HttpStatus

class CustomException(
    message: String? = null,
    val code: Int? = null,
    val status: HttpStatus? = null
) : RuntimeException(message)

class TokenNotFoundException(
    message: String? = null,
) : RuntimeException(message)