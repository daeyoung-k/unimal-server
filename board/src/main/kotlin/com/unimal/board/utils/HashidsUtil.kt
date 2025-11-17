package com.unimal.board.utils

import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.HashidsException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.hashids.Hashids
import org.springframework.stereotype.Component

@Component
class HashidsUtil(
    private val hashids: Hashids
) {

    private val logger = KotlinLogging.logger {}

    fun encode(id: Long): String {
        try {
            val hashids = hashids.encode(id)
            return hashids
        } catch (e: Exception) {
            logger.error(e) { "Hashids encode error - ${e.message}" }
            throw HashidsException(ErrorCode.HASHIDS_ENCODE_ERROR.message)
        }
    }

    fun decode(value: String): Long {
        try {
            val hashids = hashids.decode(value)
            val id = hashids.first()
            return id
        } catch (e: Exception) {
            logger.error(e) { "Hashids decode error - ${e.message}" }
            throw HashidsException(ErrorCode.HASHIDS_DECODE_ERROR.message)
        }

    }
}