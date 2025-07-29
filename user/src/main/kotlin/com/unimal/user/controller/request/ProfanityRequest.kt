package com.unimal.user.controller.request

import com.unimal.user.domain.slang.SlangType
import jakarta.validation.constraints.NotBlank

data class ProfanityRequest(
    val profanity: List<String>,
    val type: SlangType = SlangType.PROFANITY
)
