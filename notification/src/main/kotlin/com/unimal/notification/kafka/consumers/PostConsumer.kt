package com.unimal.notification.kafka.consumers

import com.unimal.common.dto.kafka.post.PostAppPushEvent
import com.unimal.notification.service.apppush.AppPushService
import com.unimal.notification.service.apppush.dto.AppPushSend
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PostConsumer(
    private val appPushService: AppPushService
) {

    @KafkaListener(topics = ["board.postAppPushTopic"], groupId = "unimal-notification-group")
    fun postAppPushConsumer(postAppPushEvent: PostAppPushEvent) {
        appPushService.sendPush(
            AppPushSend(
                token = postAppPushEvent.token,
                title = postAppPushEvent.title,
                body = postAppPushEvent.body,
                data = mapOf(
                    "type" to postAppPushEvent.type.name,
                    "target_id" to postAppPushEvent.targetId
                )
            )
        )

    }
}