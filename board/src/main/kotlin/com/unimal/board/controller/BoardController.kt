package com.unimal.board.controller

import com.unimal.board.kafka.topic.TestProducer
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.*

@RestController
class BoardController(
    private val testProducer: TestProducer
) {

    @GetMapping("/posts/list")
    fun getPostsList(): CommonResponse {
        return CommonResponse(data = "게시글 목록 조회")
    }

    @PostMapping("/posts/create")
    fun createPost(
         @UserInfoAnnotation userInfo: CommonUserInfo
        // postsCreateRequest: PostsCreateRequest,
    ): CommonResponse {
        return CommonResponse(data = "게시글 생성")
    }

    @PatchMapping("/posts/update")
    fun updatePost(): CommonResponse {
        return CommonResponse(data = "게시글 수정")
    }

    @DeleteMapping("/posts/delete")
    fun deletePost(): CommonResponse {
        return CommonResponse(data = "게시글 삭제")
    }

    @GetMapping("/kafka/test")
    fun testKafka(
        @RequestParam(value = "message", required = false) message: String?,
    ): CommonResponse {
        testProducer.testTopic(message!!)
        return CommonResponse()
    }
}