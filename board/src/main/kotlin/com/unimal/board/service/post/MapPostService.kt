package com.unimal.board.service.post

import com.unimal.board.controller.map.dto.LocationPostRequest
import com.unimal.board.domain.board.BoardRepositoryImpl
import com.unimal.board.domain.board.map.MapBoardRepositoryImpl
import com.unimal.board.service.post.dto.BoardFileInfo
import com.unimal.board.service.post.dto.map.MapPostInfo
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import org.springframework.stereotype.Service

@Service
class MapPostService(
    private val mapBoardRepositoryImpl: MapBoardRepositoryImpl,
    private val boardRepositoryImpl: BoardRepositoryImpl,

    private val hashidsUtil: HashidsUtil,
) {

    fun getLocationPosts(
        userInfo: CommonUserInfo,
        locationPostRequest: LocationPostRequest
    ): List<MapPostInfo> {
        val mapBoardList = mapBoardRepositoryImpl.findLocationPosts(
            userInfo.email,
            locationPostRequest.latitude,
            locationPostRequest.longitude,
            locationPostRequest.zoomLevel.radiusMeters,
            locationPostRequest.zoomLevel.postLimit
        )

        // N+1 방지
        val idList = mapBoardList.map { it.id.toLong() }
        val boardFiles = boardRepositoryImpl.boardFileList(idList)

        return mapBoardList.map { mapPostInfo ->
            val fileInfoList = boardFiles.mapNotNull {
                if (it.board.id.toString() == mapPostInfo.id) {
                    BoardFileInfo(fileId = hashidsUtil.encode(it.id!!), fileUrl = it.fileUrl!!)
                } else null
            }
            mapPostInfo.copy(
                id = hashidsUtil.encode(mapPostInfo.id.toLong()),
                fileInfoList = fileInfoList
            )
        }

    }
}