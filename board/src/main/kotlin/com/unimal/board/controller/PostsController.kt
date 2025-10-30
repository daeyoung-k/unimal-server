package com.unimal.board.controller

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.service.posts.PostsService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class BoardController(
    private val postsService: PostsService
) {

    @GetMapping("/posts")
    fun getPosts(): CommonResponse {
        return CommonResponse(data = "게시글 조회")
    }

    @GetMapping("/posts/list")
    fun getPostsList(): CommonResponse {
        return CommonResponse(data = "게시글 목록 조회")
    }

    @PostMapping("/posts", consumes = ["multipart/form-data"])
    fun createPost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute postsCreateRequest: PostsCreateRequest,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): CommonResponse {
        return CommonResponse(data = postsService.posting(userInfo, postsCreateRequest, files))
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