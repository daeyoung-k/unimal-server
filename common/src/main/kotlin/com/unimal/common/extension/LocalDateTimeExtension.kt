package com.unimal.common.extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.toPatternString(pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}