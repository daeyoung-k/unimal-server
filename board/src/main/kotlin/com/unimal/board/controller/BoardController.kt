package com.unimal.board.controller

import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.*

@RestController
class BoardController(
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
}