package com.unimal.common.enums

enum class Gender {
    WOMAN,
    MAN;

    companion object {
        fun from(value: String?): Gender? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }

}