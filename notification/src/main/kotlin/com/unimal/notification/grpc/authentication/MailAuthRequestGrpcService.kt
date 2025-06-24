package com.unimal.notification.grpc.authentication

import com.unimal.notification.service.NotificationService
import com.unimal.notification.service.authcode.CreateAuthCodeObject
import com.unimal.proto.notification.authentication.MailAuthRequestSendRequest
import com.unimal.proto.notification.authentication.MailAuthRequestSendResponse
import com.unimal.proto.notification.authentication.MailAuthRequestServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class MailAuthRequestGrpcService(
    private val createAuthCodeObject: CreateAuthCodeObject,
    private val notificationService: NotificationService
): MailAuthRequestServiceGrpc.MailAuthRequestServiceImplBase() {

    override fun sendMailAuthRequest(
        request: MailAuthRequestSendRequest,
        responseObserver: StreamObserver<MailAuthRequestSendResponse>
    ) {

        val email = request.email
        val authCode = createAuthCodeObject.createMailAuthCode(email)
        notificationService.authenticationCodeSendMail(email, authCode)

        val response = MailAuthRequestSendResponse.newBuilder()
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


}