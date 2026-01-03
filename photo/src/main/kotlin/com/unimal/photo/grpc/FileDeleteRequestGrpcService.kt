package com.unimal.photo.grpc

import com.unimal.photo.service.S3Service
import com.unimal.proto.photo.FileDeleteRequest
import com.unimal.proto.photo.FileDeleteResponse
import com.unimal.proto.photo.FileDeleteServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class FileDeleteRequestGrpcService(
    private val s3Service: S3Service
): FileDeleteServiceGrpc.FileDeleteServiceImplBase() {
    override fun deleteFile(
        request: FileDeleteRequest,
        responseObserver: StreamObserver<FileDeleteResponse>
    ) {
        val keys = request.keysList
        s3Service.deleteFile(keys)

        val response = FileDeleteResponse.newBuilder()
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

}