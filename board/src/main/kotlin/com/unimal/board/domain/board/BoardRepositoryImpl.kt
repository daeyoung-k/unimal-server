package com.unimal.board.domain.board

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.unimal.board.domain.board.Board_.location
import org.springframework.stereotype.Repository

@Repository
class BoardRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) {

    private val board = QBoard.board
    private val boardFile = QBoardFile.boardFile

    fun postList() {
        val boards = queryFactory.selectFrom(board)
            .where(
                locationDistance(127.086798, 37.541003, 5)
            )

        println(boards)
        val result2 = boards.fetch()
        // board 스키마에 postGis 함수들이 없어서 오류가 발생.

        println(result2)
    }

    private fun locationDistance(
        longitude: Double?,
        latitude: Double?,
        distance: Int
    ): BooleanExpression? {
        if (longitude == null || latitude == null) return null

//        select * from unimal_board.board where ST_DWithin(location, ST_MakePoint(127.086798, 37.541003), 5000)

        return Expressions.booleanTemplate(
            "ST_DWithin(${board.location}, ST_MakePoint($longitude, $latitude), ${distance * 1000})"
        )
    }
}
