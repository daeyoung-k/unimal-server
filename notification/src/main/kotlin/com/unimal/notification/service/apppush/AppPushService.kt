package com.unimal.notification.service.apppush

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.unimal.notification.service.apppush.dto.AppPushSend
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AppPushService(
    private val firebaseMessaging: FirebaseMessaging
) {

//    private val logger = LoggerFactory.getLogger(AppPushService::class.java)
    private val logger = KotlinLogging.logger {  }

    /**
     * 단일 기기 푸시 전송
     * @param token FCM 디바이스 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택)
     */
    fun sendPush(
        appPushSend: AppPushSend
    ) {
        try {
            val notification = Notification.builder()
                .setTitle(appPushSend.title)
                .setBody(appPushSend.body)
                .build()

            val messageBuilder = Message.builder()
                .setToken(appPushSend.token)
                .setNotification(notification)

            if (appPushSend.data.isNotEmpty()) {
                messageBuilder.putAllData(appPushSend.data)
            }

            val message = messageBuilder.build()
            val response = firebaseMessaging.send(message)
            
            logger.info { "Successfully sent message: $response" }
        } catch (e: Exception) {
            logger.error(e) {"Failed to send push notification to token: ${appPushSend.token}"}
            // 필요 시 커스텀 예외로 감싸서 던지거나, 재시도 로직 추가
        }
    }

    /**
     * 다중 기기 푸시 전송 (Multicast)
     * @param tokens FCM 디바이스 토큰 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     */
    fun sendMulticastPush(tokens: List<String>, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (tokens.isEmpty()) return

        // FCM MulticastMessage는 한 번에 최대 500개까지만 전송 가능하므로 청크로 분할 처리
        tokens.chunked(500).forEach { chunkedTokens ->
            try {
                val notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()

                val message = com.google.firebase.messaging.MulticastMessage.builder()
                    .addAllTokens(chunkedTokens)
                    .setNotification(notification)
                    .apply {
                        if (data.isNotEmpty()) putAllData(data)
                    }
                    .build()

                val response = firebaseMessaging.sendEachForMulticast(message)
                
                logger.info { "Batch sent. Success: ${response.successCount}, Failure: ${response.failureCount}" }

                if (response.failureCount > 0) {
                    val failedTokens = mutableListOf<String>()
                    response.responses.forEachIndexed { index, sendResponse ->
                        if (!sendResponse.isSuccessful) {
                            failedTokens.add(chunkedTokens[index])
                            // 에러 원인 로깅 (예: 유효하지 않은 토큰 등)
                            // logger.warn("Failure reason: ${sendResponse.exception.messagingErrorCode}")
                        }
                    }
                    // 실패한 토큰들에 대한 후처리 (예: DB에서 삭제 등) 로직이 필요할 수 있음
                    handleFailedTokens(failedTokens)
                }

            } catch (e: Exception) {
                logger.error(e) { "Failed to send multicast push" }
            }
        }
    }

    private fun handleFailedTokens(failedTokens: List<String>) {
        if (failedTokens.isEmpty()) return
        logger.warn { "Failed tokens found: ${failedTokens.size}. Consider removing invalid tokens from DB." }
        // TODO: 유효하지 않은 토큰(UNREGISTERED 등)을 DB에서 삭제하거나 비활성화하는 로직 연동
    }
}
