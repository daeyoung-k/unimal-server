package com.unimal.board.controller.request

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.member.BoardMember

data class PostsCreateRequest(
    val title: String? = null,
    val content: String,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
) {
    fun toBoardCreateDto(
        user: BoardMember
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
        )
    }
}