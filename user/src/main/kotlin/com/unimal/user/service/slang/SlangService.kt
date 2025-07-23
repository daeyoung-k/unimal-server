package com.unimal.user.service.slang

import com.unimal.user.domain.slang.SlangType
import com.unimal.user.utils.RedisCacheManager
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SlangService(
    private val slangObject: SlangObject,
    private val redisCacheManager: RedisCacheManager
) {
    val logger = KotlinLogging.logger {}

    @Transactional
    fun insertProfanity(profanityList: List<String>) {

        if (profanityList.isEmpty()) {
            logger.info { "비속어 목록이 비어 있습니다." }
            return
        }

        profanityList.forEach { profanity ->
            slangObject.getProfanitySlang(profanity)?.let {
                return@forEach
            } ?: run {
                slangObject.profanitySaved(profanity)
            }
        }

        val slangProfanityList = slangObject.getProfanityList()

        redisCacheManager.addCache(SlangType.PROFANITY.name, slangProfanityList)
        val cacheProfanity = redisCacheManager.getCacheSet(SlangType.PROFANITY.name)
        logger.info {
            "비속어 캐시 업데이트 완료: ${cacheProfanity.size}개의 비속어가 캐시에 저장되었습니다."
        }
    }

}