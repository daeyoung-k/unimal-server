package com.unimal.board.controller.request.post

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.member.BoardMember
import com.unimal.board.enums.MapShow
import com.unimal.board.enums.PostShow
import org.locationtech.jts.geom.Point

data class PostCreateRequest(
    val title: String? = null,
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isShow: PostShow = PostShow.PUBLIC,
    val isMapShow: MapShow = MapShow.SAME,
) {
    fun toBoardCreateDto(
        user: BoardMember,
        location: Point? = null,
    ): Board {
        return Board(
            email = user,
            title = title,
            content = content,
            streetName = streetName,
            postalCode = postalCode,
            siDo = siDo,
            guGun = guGun,
            dong = dong,
            location = location,
            show = isShow,
            mapShow = isMapShow
        )
    }
}