package com.unimal.board.grpc.service

import com.unimal.proto.photo.PhotoGetRequest
import com.unimal.proto.photo.PhotoGetResponse
import com.unimal.proto.photo.PhotoServiceGrpc
import org.springframework.stereotype.Service

@Service
class PhotoGrpcService(
    private val photoBlockingStub: PhotoServiceGrpc.PhotoServiceBlockingStub
) {

    fun getPhotoGrpc(): String {
        val request = PhotoGetRequest.newBuilder()
            .setPhoto("Sample photo data")
            .build()

        val response: PhotoGetResponse = photoBlockingStub.getPhoto(request)
        return response.photos
    }
}