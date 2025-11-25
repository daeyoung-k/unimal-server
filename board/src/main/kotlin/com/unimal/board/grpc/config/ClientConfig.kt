package com.unimal.board.grpc.config

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientConfig {

    @Bean
    fun photoGrpcChannel(): ManagedChannel =
        ManagedChannelBuilder.forAddress("localhost", 50084) // photo 모듈의 gRPC 서버 주소와 포트
            .usePlaintext()
            .build()

}