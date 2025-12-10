package com.unimal.board.controller.request

import com.unimal.board.enums.MapShow
import com.unimal.board.enums.PostShow

data class PostUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val isShow: PostShow? = null,
    val isMapShow: MapShow? = null,
)


