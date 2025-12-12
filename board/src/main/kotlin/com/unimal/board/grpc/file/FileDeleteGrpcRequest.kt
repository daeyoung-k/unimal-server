package com.unimal.board.grpc.file

import com.unimal.proto.photo.FileDeleteRequest
import com.unimal.proto.photo.FileDeleteServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class FileDeleteGrpcRequest {
    @GrpcClient("photo-grpc")
    lateinit var fileDeleteServiceGrpc: FileDeleteServiceGrpc.FileDeleteServiceBlockingStub

    fun deleteFile(keys: List<String>) {



        val request = FileDeleteRequest.newBuilder()
            .addAllKeys(keys)
            .build()

        fileDeleteServiceGrpc.deleteFile(request)
    }

}