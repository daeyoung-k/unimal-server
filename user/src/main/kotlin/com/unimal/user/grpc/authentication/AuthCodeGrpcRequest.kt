package com.unimal.user.grpc.authentication

import com.unimal.proto.notification.authentication.MailAuthRequestSendRequest
import com.unimal.proto.notification.authentication.MailAuthRequestServiceGrpc
import com.unimal.proto.notification.authentication.TelAuthRequestSendRequest
import com.unimal.proto.notification.authentication.TelAuthRequestServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class AuthCodeGrpcRequest {

    @GrpcClient("notification-grpc")
    lateinit var mailAuthRequestServiceGrpc: MailAuthRequestServiceGrpc.MailAuthRequestServiceBlockingStub

    @GrpcClient("notification-grpc")
    lateinit var telAuthRequestServiceGrpc: TelAuthRequestServiceGrpc.TelAuthRequestServiceBlockingStub

    fun sendMailAuthRequest(key: String, email: String) {
        val request = MailAuthRequestSendRequest.newBuilder()
            .setKey(key)
            .setEmail(email)
            .build()

        mailAuthRequestServiceGrpc.sendMailAuthRequest(request)
    }

    fun sendTelAuthRequest(key: String, tel: String) {
        val request = TelAuthRequestSendRequest.newBuilder()
            .setKey(key)
            .setTel(tel)
            .build()
        telAuthRequestServiceGrpc.sendTelAuthRequest(request)
    }
}