package com.unimal.user.grpc.authentication

import com.unimal.proto.notification.authentication.MailAuthRequestSendRequest
import com.unimal.proto.notification.authentication.MailAuthRequestServiceGrpc
import com.unimal.proto.notification.authentication.TelAuthRequestSendRequest
import com.unimal.proto.notification.authentication.TelAuthRequestServiceGrpc
import com.unimal.proto.notification.authentication.TelFindAuthRequestSendRequest
import com.unimal.proto.notification.authentication.TelFindAuthRequestServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class AuthCodeGrpcRequest {

    @GrpcClient("notification-grpc")
    lateinit var mailAuthRequestServiceGrpc: MailAuthRequestServiceGrpc.MailAuthRequestServiceBlockingStub

    @GrpcClient("notification-grpc")
    lateinit var telAuthRequestServiceGrpc: TelAuthRequestServiceGrpc.TelAuthRequestServiceBlockingStub

    @GrpcClient("notification-grpc")
    lateinit var telFindAuthRequestServiceGrpc: TelFindAuthRequestServiceGrpc.TelFindAuthRequestServiceBlockingStub

    fun sendMailAuthRequest(email: String) {
        val request = MailAuthRequestSendRequest.newBuilder()
            .setEmail(email)
            .build()

        mailAuthRequestServiceGrpc.sendMailAuthRequest(request)
    }

    fun sendTelAuthRequest(email: String, tel: String) {
        val request = TelAuthRequestSendRequest.newBuilder()
            .setEmail(email)
            .setTel(tel)
            .build()
        telAuthRequestServiceGrpc.sendTelAuthRequest(request)
    }

    fun sendTelFindAuthRequest(tel: String) {
        val request = TelFindAuthRequestSendRequest.newBuilder()
            .setTel(tel)
            .build()
        telFindAuthRequestServiceGrpc.sendTelFindAuthRequest(request)
    }
}