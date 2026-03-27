package com.unimal.common.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toPatternLocalDateTime(pattern: String): LocalDateTime {
    val format = DateTimeFormatter.ofPattern(pattern)
    return LocalDateTime.parse(this, format)
}

fun String.toPatternLocalDate(pattern: String): LocalDate {
    val format = DateTimeFormatter.ofPattern(pattern)
    return LocalDate.parse(this, format)
}