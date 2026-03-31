package com.unimal.board.controller.post

import com.unimal.board.controller.post.dto.MyPostListRequest
import com.unimal.board.controller.post.dto.PostCreateRequest
import com.unimal.board.controller.post.dto.PostFileDeleteRequest
import com.unimal.board.controller.post.dto.PostListRequest
import com.unimal.board.controller.post.dto.PostReplyRequest
import com.unimal.board.controller.post.dto.PostUpdateRequest
import com.unimal.board.service.post.PostCalculateService
import com.unimal.board.service.post.PostService
import com.unimal.common.annotation.user.OptionalUserInfoAnnotation
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/post")
class PostController(
    private val postService: PostService,
    private val postCalculateService: PostCalculateService,
) {

    @GetMapping("/{boardId}")
    fun getPost(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.getPost(optionalUserInfo, boardId))
    }

    @GetMapping("/list")
    fun getPostList(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @ModelAttribute postListRequest: PostListRequest
    ): CommonResponse {
        return CommonResponse(data = postService.getPostList(optionalUserInfo, postListRequest))
    }

    @GetMapping("/my/list")
    fun getMyPostList(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute myPostListRequest: MyPostListRequest
    ): CommonResponse {
        return CommonResponse(data = postService.getMyPostList(userInfo, myPostListRequest))
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createPost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute postCreateRequest: PostCreateRequest,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): CommonResponse {
        return CommonResponse(data = postService.posting(userInfo, postCreateRequest, files))
    }

    @PatchMapping("/{boardId}/update")
    fun updatePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody postUpdateRequest: PostUpdateRequest
    ): CommonResponse {
        return CommonResponse(data = postService.postUpdate(userInfo, boardId, postUpdateRequest))
    }

    @PostMapping("/{boardId}/file/upload", consumes = ["multipart/form-data"])
    fun uploadPostFile(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestPart("files", required = false) files: List<MultipartFile>,
    ): CommonResponse {
        return CommonResponse(data = postService.postFileUpload(userInfo, boardId, files))
    }

    @PostMapping("/{boardId}/file/delete")
    fun deletePostFile(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody postFileDeleteRequest: PostFileDeleteRequest
    ): CommonResponse {
        return CommonResponse(data = postService.postFileDelete(userInfo, boardId, postFileDeleteRequest))
    }

    @DeleteMapping("/{boardId}/delete")
    fun deletePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.postDelete(userInfo, boardId))
    }

    @GetMapping("/{boardId}/like")
    fun likePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.postLike(userInfo, boardId))
    }

    @PostMapping("/{boardId}/reply")
    fun createReply(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = postService.replyCreate(userInfo, boardId, postReplyRequest))
    }

    @GetMapping("/{boardId}/reply")
    fun getReplyList(
        @OptionalUserInfoAnnotation optionalUserInfo: CommonUserInfo?,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.replyList(optionalUserInfo, boardId))
    }

    @PatchMapping("/{boardId}/reply/{replyId}/update")
    fun replyUpdate(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
        @RequestBody @Valid postReplyRequest: PostReplyRequest
    ): CommonResponse {
        return CommonResponse(data = postService.replyUpdate(userInfo, boardId, replyId, postReplyRequest))
    }

    @DeleteMapping("/{boardId}/reply/{replyId}/delete")
    fun replyDelete(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
        @PathVariable replyId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.replyDelete(userInfo, boardId, replyId))
    }


    @GetMapping("/total/like")
    fun totalLikeCount(
        @UserInfoAnnotation userInfo: CommonUserInfo,
    ): CommonResponse {
        return CommonResponse(data = postCalculateService.getLikeTotalCount(userInfo.email))
    }

    @GetMapping("/total")
    fun totalCount(
        @UserInfoAnnotation userInfo: CommonUserInfo,
    ): CommonResponse {
        return CommonResponse(data = postCalculateService.getPostTotalCount(userInfo.email))
    }
}