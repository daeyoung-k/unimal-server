package com.unimal.photo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.transfer.s3.S3TransferManager

@Configuration
class S3Config(
    @Value("\${aws.access-key}")
    private val accessKey: String,
    @Value("\${aws.secret-key}")
    private val secretKey: String,
    @Value("\${aws.s3.region}")
    private val s3Region: String,
) {

    @Bean
    fun s3Client(): S3Client = S3Client.builder()
            .region(Region.of(s3Region))
            .credentialsProvider {  AwsBasicCredentials.create(accessKey, secretKey) }
            .build()

    @Bean
    fun s3Presigner(): S3Presigner = S3Presigner.builder()
        .region(Region.of(s3Region))
        .credentialsProvider { AwsBasicCredentials.create(accessKey, secretKey) }
        .build()

    @Bean(destroyMethod = "close") // 종료 시 자원 정리 보장 스위치.
    fun s3AsyncClient(): S3AsyncClient {
        // CRT 기반 고성능 비동기 클라이언트
        return S3AsyncClient.crtBuilder()
            .region(Region.of(s3Region))
            .targetThroughputInGbps(10.0)    // 10Gbps 네트워크 대역폭을 목표로 삼아 “동시에 더 많은 파트”를 올리도록 조정.
            .minimumPartSizeInBytes(8L * 1024 * 1024) // 멀티파트 최소 파트 크기 (8MB)
            .build()
    }

    @Bean
    fun s3TransferManager(): S3TransferManager = S3TransferManager.builder()
        .s3Client(s3AsyncClient())
        .build()
}