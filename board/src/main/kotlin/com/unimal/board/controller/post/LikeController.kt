package com.unimal.board.controller.post

import com.unimal.board.service.post.PostCalculateService
import com.unimal.board.service.post.PostService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/post")
class LikeController(
    private val postService: PostService,
    private val postCalculateService: PostCalculateService,
) {

    @GetMapping("/{boardId}/like")
    fun likePost(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @PathVariable boardId: String,
    ): CommonResponse {
        return CommonResponse(data = postService.postLike(userInfo, boardId))
    }

    @GetMapping("/total/like")
    fun totalLikeCount(
        @UserInfoAnnotation userInfo: CommonUserInfo,
    ): CommonResponse {
        return CommonResponse(data = postCalculateService.getLikeTotalCount(userInfo.email))
    }

    @GetMapping("/like/stories/total")
    fun totalLikeStoriesCount(
        @UserInfoAnnotation userInfo: CommonUserInfo,
    ): CommonResponse {
        return CommonResponse(data = postCalculateService.getLikedStoriesCount(userInfo.email))
    }
}