package com.unimal.photo.grpc

import com.unimal.grpc.photo.GetEmptyRequest
import com.unimal.grpc.photo.GetRequest
import com.unimal.grpc.photo.GetResponse
import com.unimal.grpc.photo.PhotoServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PhotoGrpcService: PhotoServiceGrpc.PhotoServiceImplBase() {

    override fun getPhoto(
        request: GetRequest,
        responseObserver: StreamObserver<GetResponse>
    ) {
        val boardData = request.photo.toByteArray() // ByteString → ByteArray

        // 비즈니스 로직 예제 (예: board 데이터를 문자열로 변환)
        val boardString = String(boardData)

        // 예: 가공 후의 결과 데이터
        val responseString = "받은 photo 데이터: $boardString"

        // 응답 메시지 생성
        val response = GetResponse.newBuilder()
            .setPhotos(responseString)
            .build()

        // 응답 전송
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getPhotoService(
        request: GetEmptyRequest?,
        responseObserver: StreamObserver<GetResponse>
    ) {
        // 응답 메시지 생성
        val response = GetResponse.newBuilder()
            .setPhotos("Hello, Photo Service!")
            .build()

        // 응답 전송
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}