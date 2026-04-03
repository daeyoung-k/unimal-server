package com.unimal.board.service.post

import com.unimal.board.controller.map.dto.LocationPostRequest
import com.unimal.board.domain.board.map.MapBoardRepositoryImpl
import com.unimal.board.service.post.dto.map.MapPostInfo
import com.unimal.common.dto.CommonUserInfo
import org.springframework.stereotype.Service

@Service
class MapPostService(
    private val mapBoardRepositoryImpl: MapBoardRepositoryImpl,
) {

    fun getLocationPosts(
        userInfo: CommonUserInfo,
        locationPostRequest: LocationPostRequest
    ): List<MapPostInfo> {
        return mapBoardRepositoryImpl.findLocationPosts(
            userInfo.email,
            locationPostRequest.latitude,
            locationPostRequest.longitude,
            locationPostRequest.zoomLevel.radiusMeters,
            locationPostRequest.zoomLevel.postLimit
        )

    }
}