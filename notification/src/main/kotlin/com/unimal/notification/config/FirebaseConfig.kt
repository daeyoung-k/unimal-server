package com.unimal.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FirebaseConfig(
    @Value("\${custom.firebase.adminsdk}")
    private val firebaseAdminSdk: String
) {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val firebaseApp = getFirebaseApp()
        return FirebaseMessaging.getInstance(firebaseApp)
    }

    private fun getFirebaseApp(): FirebaseApp {
        val firebaseApps = FirebaseApp.getApps()
        // 이미 초기화된 앱이 있으면 반환, 없으면 새로 초기화
        return firebaseApps.firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: run {
                logger.info("FirebaseApp 초기화를 시작합니다.")
                // resources/firebase 디렉토리 하위의 json 파일을 로드
                val resource = ClassPathResource("firebase/$firebaseAdminSdk.json")

                if (!resource.exists()) {
                    logger.error("Firebase Admin SDK JSON 파일을 찾을 수 없습니다.")
                    throw IllegalStateException("Firebase Admin SDK JSON 파일을 찾을 수 없습니다.")
                }

                val options = resource.inputStream.use { inputStream ->
                    FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build()
                }

                FirebaseApp.initializeApp(options)
            }
    }
}
