package com.unimal.board.controller.request.post

import com.unimal.board.controller.enums.PostSortType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

data class MyPostListRequest(
    val keyword: String? = null,
    val sortType: PostSortType = PostSortType.LATEST,
    val page: Int = 0,
    val size: Int = 20
) {
    val pageable: Pageable get() = PageRequest.of(page, size)
}
