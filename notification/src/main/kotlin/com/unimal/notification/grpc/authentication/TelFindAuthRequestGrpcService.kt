package com.unimal.notification.grpc.authentication

import com.unimal.notification.service.NotificationService
import com.unimal.proto.notification.authentication.TelFindAuthRequestSendRequest
import com.unimal.proto.notification.authentication.TelFindAuthRequestSendResponse
import com.unimal.proto.notification.authentication.TelFindAuthRequestServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class TelFindAuthRequestGrpcService(
    private val notificationService: NotificationService,
): TelFindAuthRequestServiceGrpc.TelFindAuthRequestServiceImplBase() {
    override fun sendTelFindAuthRequest(
        request: TelFindAuthRequestSendRequest,
        responseObserver: StreamObserver<TelFindAuthRequestSendResponse>
    ) {
        val tel = request.tel
        notificationService.telFindAuthCodeSend(tel)
        val response = TelFindAuthRequestSendResponse.newBuilder()
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}