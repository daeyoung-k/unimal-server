package com.unimal.board.controller.map

import com.unimal.board.controller.map.dto.LocationPostRequest
import com.unimal.board.service.post.MapPostService
import com.unimal.common.annotation.user.UserInfoAnnotation
import com.unimal.common.dto.CommonResponse
import com.unimal.common.dto.CommonUserInfo
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/map")
class MapController(
    private val mapPostService: MapPostService
) {

    @GetMapping("/location/post")
    fun locationPostList(
        @UserInfoAnnotation userInfo: CommonUserInfo,
        @ModelAttribute @Valid locationPostRequest: LocationPostRequest
    ): CommonResponse {
        return CommonResponse(data = mapPostService.getLocationPosts(userInfo, locationPostRequest))
    }
}