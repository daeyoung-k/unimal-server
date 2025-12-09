package com.unimal.common.enums

enum class TokenType(
    val description: String,
    val cacheKey: String?
) {
    ACCESS("액세스 토큰", "access-token"),
    REFRESH("리프레쉬 토큰", null),

    ;

    companion object {
        fun from(value: String?): TokenType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}