package com.unimal.photo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

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
}