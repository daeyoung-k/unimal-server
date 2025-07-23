package com.unimal.user.service.slang

import com.unimal.user.domain.slang.Slang
import com.unimal.user.domain.slang.SlangRepository
import com.unimal.user.domain.slang.SlangType
import org.springframework.stereotype.Component

@Component
class SlangObject(
    private val slangRepository: SlangRepository
) {
    fun profanitySaved(profanity: String) {
        slangRepository.save(
            Slang(
                slang = profanity,
                type = SlangType.PROFANITY,
            )
        )

    }

    fun getProfanitySlang(slang: String): Slang? {
        return slangRepository.findBySlangAndType(slang)
    }

    fun getProfanityList(): List<String> = slangRepository.findSlangTextsByType(SlangType.PROFANITY)
}