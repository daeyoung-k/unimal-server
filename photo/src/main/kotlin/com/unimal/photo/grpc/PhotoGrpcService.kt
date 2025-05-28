package com.unimal.photo.grpc


import com.unimal.proto.photo.PhotoGetEmptyRequest
import com.unimal.proto.photo.PhotoGetRequest
import com.unimal.proto.photo.PhotoGetResponse
import com.unimal.proto.photo.PhotoServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PhotoGrpcService: PhotoServiceGrpc.PhotoServiceImplBase() {

    override fun getPhoto(
        request: PhotoGetRequest,
        responseObserver: StreamObserver<PhotoGetResponse>
    ) {
        val photoData = request.photo.toByteArray() // ByteString → ByteArray

        // 비즈니스 로직 예제 (예: photo 데이터를 문자열로 변환)
        val photoString = String(photoData)

        // 예: 가공 후의 결과 데이터
        val responseString = "받은 photo 데이터: $photoString"

        // 응답 메시지 생성
        val response = PhotoGetResponse.newBuilder()
            .setPhotos(responseString)
            .build()

        // 응답 전송
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getPhotoService(
        request: PhotoGetEmptyRequest?,
        responseObserver: StreamObserver<PhotoGetResponse>
    ) {
        // 응답 메시지 생성
        val response = PhotoGetResponse.newBuilder()
            .setPhotos("Hello, Photo Service!")
            .build()

        // 응답 전송
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}