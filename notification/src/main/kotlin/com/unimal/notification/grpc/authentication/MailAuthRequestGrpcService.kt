package com.unimal.notification.grpc.authentication

import com.unimal.notification.service.NotificationService
import com.unimal.proto.notification.authentication.MailAuthRequestSendRequest
import com.unimal.proto.notification.authentication.MailAuthRequestSendResponse
import com.unimal.proto.notification.authentication.MailAuthRequestServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class MailAuthRequestGrpcService(
    private val notificationService: NotificationService
): MailAuthRequestServiceGrpc.MailAuthRequestServiceImplBase() {

    override fun sendMailAuthRequest(
        request: MailAuthRequestSendRequest,
        responseObserver: StreamObserver<MailAuthRequestSendResponse>
    ) {

        val email = request.email
        notificationService.mailAuthenticationCodeSend(email)

        val response = MailAuthRequestSendResponse.newBuilder()
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


}