package com.unimal.board.controller.request.post

import com.unimal.board.controller.enums.PostSortType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

data class PostListRequest(
    // 내근처
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance: Double = 5.0, // 5km

    val keyword: String? = null,
    val sortType: PostSortType = PostSortType.LATEST,

    val page: Int = 0,
    val size: Int = 10
) {
    // 객체 생성 시점에 계산 후 고정됨.
//    val isLocationSearch = latitude != null && longitude != null

    // 호출할 때마다 매번 계산됨.
    val isLocationSearch get() =  latitude != null && longitude != null

    val pageable: Pageable get() = PageRequest.of(page, size)
}
