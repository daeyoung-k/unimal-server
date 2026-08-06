package com.unimal.board.service.post

import com.unimal.board.controller.map.dto.LocationPostRequest
import com.unimal.board.domain.board.BoardRepositoryImpl
import com.unimal.board.domain.board.map.MapBoardRepositoryImpl
import com.unimal.board.service.post.dto.BoardFileInfo
import com.unimal.board.service.post.dto.map.MapPostInfo
import com.unimal.board.service.share.ShareUrlFactory
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import org.springframework.stereotype.Service

@Service
class MapPostService(
    private val mapBoardRepositoryImpl: MapBoardRepositoryImpl,
    private val boardRepositoryImpl: BoardRepositoryImpl,

    private val hashidsUtil: HashidsUtil,
    private val shareUrlFactory: ShareUrlFactory,
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

    fun mapPosts(
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
                    BoardFileInfo(fileId = hashidsUtil.encode(it.id!!), fileUrl = it.fileUrl!!, thumbUrl = it.thumbUrl)
                } else null
            }
            val encodedId = hashidsUtil.encode(mapPostInfo.id.toLong())
            mapPostInfo.copy(
                id = encodedId,
                fileInfoList = fileInfoList,
                // hashid 를 채우는 자리에서 같이 채운다. 공유 URL 은 인코딩된 ID 를
                // 쓰므로 이 두 값은 항상 함께 세팅돼야 한다 — 따로 두면 한쪽만
                // 채워진 응답이 나갈 수 있다.
                //
                // 마커 쿼리가 PUBLIC 만 뽑으므로 여기서는 무조건 공유 가능하다.
                // 공개 여부를 다시 따지는 ofShareable() 이 아니라 of() 를 쓰는 이유다.
                shareUrl = shareUrlFactory.of(encodedId),
            )
        }

    }
}