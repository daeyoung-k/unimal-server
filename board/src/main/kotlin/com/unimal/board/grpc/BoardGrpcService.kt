package com.unimal.board.grpc

import com.unimal.board.service.BoardService
import com.unimal.grpc.board.BoardServiceGrpc
import com.unimal.grpc.board.GetEmptyRequest
import com.unimal.grpc.board.GetRequest
import com.unimal.grpc.board.GetResponse
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class BoardGrpcService(
    private val boardService: BoardService
): BoardServiceGrpc.BoardServiceImplBase() {
    override fun getBoard(request: GetRequest, responseObserver: StreamObserver<GetResponse>) {
        // 요청 메시지에서 board 데이터를 추출
        val boardData = request.board.toByteArray() // ByteString → ByteArray

        // 비즈니스 로직 예제 (예: board 데이터를 문자열로 변환)
        val boardString = String(boardData)

        // 예: 가공 후의 결과 데이터
        val responseString = "받은 board 데이터: $boardString"

        // 응답 메시지 생성
        val response = GetResponse.newBuilder()
            .setBoards(responseString)
            .build()

        // 응답 전송
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getBoardService(request: GetEmptyRequest, responseObserver: StreamObserver<GetResponse>) {
        // 새로운 서비스 로직
        val dataFromService = boardService.testService() // BoardService의 메서드 호출

        val response = GetResponse.newBuilder()
            .setBoards(dataFromService)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}