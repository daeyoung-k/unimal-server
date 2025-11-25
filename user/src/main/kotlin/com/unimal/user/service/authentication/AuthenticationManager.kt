package com.unimal.user.service.authentication

import com.unimal.user.utils.RedisCacheManager
import com.unimal.webcommon.exception.AuthCodeException
import com.unimal.webcommon.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class AuthenticationManager(
    private val redisCacheManager: RedisCacheManager,
    @Value("\${custom.auth.master-code}")
    private val masterCode: String
) {

    fun getAuthCode(key: String): String {
        return redisCacheManager.getCache(key) ?: throw AuthCodeException(ErrorCode.AUTH_CODE_NOT_FOUND.message)
    }

    fun setAuthCodeSuccess(key: String) {
        redisCacheManager.setStringCacheMinutes(key, "SUCCESS", 10)
    }

    fun authCodeVerify(authCode: String, requestCode: String) {
        if (requestCode == masterCode) return
        if (authCode != requestCode) throw AuthCodeException(ErrorCode.AUTH_CODE_NOT_MATCH.message)
    }
}