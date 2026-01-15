package com.unimal.notification.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.notification.service.apppush.AppPushService
import com.unimal.notification.service.apppush.dto.AppPushSend
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class NotificationController(
    private val appPushService: AppPushService
) {

    @PostMapping("/app-push")
    fun appPush(
        @RequestBody appPushSend: AppPushSend
    ): CommonResponse {
        appPushService.sendPush(appPushSend)
        return CommonResponse()
    }
}