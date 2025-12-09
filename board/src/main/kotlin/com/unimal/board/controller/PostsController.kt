package com.unimal.board.controller

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.controller.request.PostsListRequest
import com.unimal.board.service.posts.PostsService
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.annotation.user.OptionalUserInfoAnnotation
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class BoardController(
    private val postsService: PostsService,
    private val hashidsUtil: HashidsUtil,
) {

    @GetMapping("/hashids")
    fun hashidsValue(
        @RequestParam("value") value: Long
    ): CommonResponse {
        return CommonResponse(data = hashidsUtil.encode(value))
    }

    @GetMapping("/post/{boardId}")
    fun getPosts(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @PathVariable("boardId") boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postsService.getPost(optionalUserInfo, boardId))
    }

    @GetMapping("/posts/list")
    fun getPostsList(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @ModelAttribute postsListRequest: PostsListRequest
    ): CommonResponse {
        return CommonResponse(data = postsService.getPostList(optionalUserInfo, postsListRequest))
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

    @GetMapping("/posts/like/{boardId}")
    fun likePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable("boardId") boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postsService.postLike(userInfo, boardId))
    }


}