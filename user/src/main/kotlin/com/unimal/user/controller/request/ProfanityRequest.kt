package com.unimal.user.controller.request

import com.unimal.user.domain.slang.SlangType
import jakarta.validation.constraints.NotBlank

data class ProfanityRequest(
    @field:NotBlank
    val profanity: String,
    val type: SlangType = SlangType.PROFANITY
)
