package com.unimal.board.controller.post

import com.unimal.board.controller.post.dto.PostReplyRequest
import com.unimal.board.service.post.PostCalculateService
import com.unimal.board.service.post.PostService
import com.unimal.common.annotation.user.OptionalUserInfoAnnotation
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/post")
class ReplyController(
    private val postService: PostService,
    private val postCalculateService: PostCalculateService,
) {
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
}