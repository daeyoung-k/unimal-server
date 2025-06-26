package com.unimal.board.controller

import com.unimal.board.controller.request.ImageMetadata
import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.service.BoardService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class BoardController(
    private val boardService: BoardService
) {

    @GetMapping("/posts/list")
    fun getPostsList(): CommonResponse {
        return CommonResponse(data = "게시글 목록 조회")
    }

    @PostMapping("/posts/create", consumes = ["multipart/form-data"])
    fun createPost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute postsCreateRequest: PostsCreateRequest,
        @RequestPart("imageMetadata") imageMetadata: List<ImageMetadata>?,
        @RequestPart("images", required = false) images: List<MultipartFile>?,
    ): CommonResponse {
        boardService.createPost(userInfo, postsCreateRequest, imageMetadata, images)
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