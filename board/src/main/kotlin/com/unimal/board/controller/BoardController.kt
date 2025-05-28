package com.unimal.board.controller

import com.unimal.board.grpc.service.PhotoGrpcService
import com.unimal.common.TestDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController(
    private val photoGrpcService: PhotoGrpcService
) {

    @GetMapping("/test")
    fun test(): TestDTO {

        return TestDTO(3)
    }

    @GetMapping("/grpc-test")
    fun grpcTest(): String {

        return photoGrpcService.getPhotoGrpc()
    }
}