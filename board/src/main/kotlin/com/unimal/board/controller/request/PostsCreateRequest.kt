package com.unimal.board.controller.request

import org.springframework.web.multipart.MultipartFile

data class PostsCreateRequest(
    val images: List<MultipartFile>,
    val title: String,
    val content: String,
)
