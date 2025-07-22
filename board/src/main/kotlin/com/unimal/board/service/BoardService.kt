package com.unimal.board.service

import com.unimal.board.controller.request.ImageMetadata
import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.common.dto.CommonUserInfo
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class BoardService {

    fun createPost(
        userInfo: CommonUserInfo,
        postsCreateRequest: PostsCreateRequest,
        imageMetadata: List<ImageMetadata>?,
        images: List<MultipartFile>?
    ) {

    }


}