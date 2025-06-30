package com.unimal.notification.grpc.authentication

import com.unimal.notification.service.NotificationService
import com.unimal.proto.notification.authentication.TelAuthRequestSendRequest
import com.unimal.proto.notification.authentication.TelAuthRequestSendResponse
import com.unimal.proto.notification.authentication.TelAuthRequestServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class TelAuthRequestGrpcService(
    private val notificationService: NotificationService,
): TelAuthRequestServiceGrpc.TelAuthRequestServiceImplBase() {
    override fun sendTelAuthRequest(
        request: TelAuthRequestSendRequest,
        responseObserver: StreamObserver<TelAuthRequestSendResponse>
    ) {
        val email = request.email
        val tel = request.tel

        notificationService.telAuthenticationCodeSend(email, tel)

        val response = TelAuthRequestSendResponse.newBuilder()
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}