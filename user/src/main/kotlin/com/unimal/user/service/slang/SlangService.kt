package com.unimal.user.service.slang

import com.unimal.user.domain.slang.SlangType
import com.unimal.user.utils.RedisCacheManager
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class SlangService(
    private val slangObject: SlangObject,
    private val redisCacheManager: RedisCacheManager
) {
    val logger = KotlinLogging.logger {}

    @Transactional
    fun insertProfanity(profanity: String) {
        try {
            slangObject.profanitySaved(profanity)
        } catch (e: DataIntegrityViolationException) {
            val message = e.message ?: ""
            if (message.contains("duplicate key value") && message.contains("slang_slang_key")) {
                logger.warn { "중복된 비속어 입력 시도: $profanity" }
            }
            logger.error(e) { "비속어 저장 중 예외 발생 - $profanity" }
            throw e
        }
        redisCacheManager.addCache(SlangType.PROFANITY.name, profanity)
        val cacheProfanity = redisCacheManager.getCacheSet(SlangType.PROFANITY.name)
        logger.info {
            "비속어 캐시 업데이트 완료 - $profanity : ${cacheProfanity.size}개의 비속어가 캐시에 저장되었습니다."
        }
    }

}