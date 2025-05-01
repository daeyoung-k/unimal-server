package com.unimal.apigateway.exception

class CustomException(
    message: String? = null,
) : RuntimeException(message)

class TokenNotFoundException(
    message: String? = null,
) : RuntimeException(message)