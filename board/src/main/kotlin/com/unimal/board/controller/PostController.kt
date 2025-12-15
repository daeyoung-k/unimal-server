package com.unimal.board.controller

import com.unimal.board.controller.request.post.PostReplyRequest
import com.unimal.board.controller.request.post.PostUpdateRequest
import com.unimal.board.controller.request.post.PostCreateRequest
import com.unimal.board.controller.request.post.PostFileDeleteRequest
import com.unimal.board.controller.request.post.PostListRequest
import com.unimal.board.service.post.PostService
import com.unimal.common.annotation.user.OptionalUserInfoAnnotation
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class PostController(
    private val postService: PostService,
) {

    @GetMapping("/post/{boardId}")
    fun getPost(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.getPost(optionalUserInfo, boardId))
    }

    @GetMapping("/post/list")
    fun getPostList(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @ModelAttribute postListRequest: PostListRequest
    ): CommonResponse {
        return CommonResponse(data = postService.getPostList(optionalUserInfo, postListRequest))
    }

    @PostMapping("/post", consumes = ["multipart/form-data"])
    fun createPost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute postCreateRequest: PostCreateRequest,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): CommonResponse {
        return CommonResponse(data = postService.posting(userInfo, postCreateRequest, files))
    }

    @PatchMapping("/post/{boardId}/update")
    fun updatePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody postUpdateRequest: PostUpdateRequest
    ): CommonResponse {
        return CommonResponse(data = postService.postUpdate(userInfo, boardId, postUpdateRequest))
    }

    @PostMapping("/post/{boardId}/file/upload", consumes = ["multipart/form-data"])
    fun uploadPostFile(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestPart("files", required = false) files: List<MultipartFile>,
    ): CommonResponse {
        return CommonResponse(data = postService.postFileUpload(userInfo, boardId, files))
    }

    @PostMapping("/post/{boardId}/file/delete")
    fun deletePostFile(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody postFileDeleteRequest: PostFileDeleteRequest
    ): CommonResponse {
        return CommonResponse(data = postService.postFileDelete(userInfo, boardId, postFileDeleteRequest))
    }

    @DeleteMapping("/post/{boardId}/delete")
    fun deletePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = "게시글 삭제")
    }

    @GetMapping("/post/{boardId}/like")
    fun likePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.postLike(userInfo, boardId))
    }

    @PostMapping("/post/{boardId}/reply")
    fun reply(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = "댓글 달기")
    }

    @PatchMapping("/post/{boardId}/reply/{replyId}/update")
    fun replyUpdate(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = "댓글 수정")
    }

    @DeleteMapping("/post/{boardId}/reply/{replyId}/delete")
    fun replyDelete(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
    ): CommonResponse {
        return CommonResponse(data = "댓글 삭제")
    }

    @PostMapping("/post/{boardId}/reply/{replyId}/rereply")
    fun reReply(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = "대댓글 달기")
    }

    @PatchMapping("/post/{boardId}/reply/{replyId}/rereply/{reReplyId}/update")
    fun reReplyUpdate(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
        @PathVariable reReplyId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = "대댓글 수정")
    }

    @DeleteMapping("/post/{boardId}/reply/{replyId}/rereply/{reReplyId}/delete")
    fun reReplyDelete(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
        @PathVariable reReplyId: String
    ): CommonResponse {
        return CommonResponse(data = "대댓글 삭제")
    }

}