package com.unimal.board.controller.request

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.member.BoardMember
import org.locationtech.jts.geom.Point

data class PostsCreateRequest(
    val title: String? = null,
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
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
            location = location
        )
    }
}