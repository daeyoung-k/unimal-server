package com.unimal.board.controller.map.dto

import com.unimal.board.service.post.enums.ZoomLevel

data class LocationPostRequest(
    val latitude: Double,
    val longitude: Double,
    val zoom: Int,
) {
    val zoomLevel = ZoomLevel.from(zoom)
}
