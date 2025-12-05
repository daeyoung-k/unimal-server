package com.unimal.board.domain.board

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.unimal.board.controller.request.PostsListRequest
import org.springframework.stereotype.Repository

@Repository
class BoardRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) {

    private val board = QBoard.board
    private val boardFile = QBoardFile.boardFile

    fun postList(
        postsListRequest: PostsListRequest
    ) {
        val distance = postsListRequest.distance
        val longitude = postsListRequest.longitude
        val latitude = postsListRequest.latitude

        val boards = queryFactory.selectFrom(board)
            .where(
                locationDistance(longitude, latitude, distance)
            )

        val result2 = boards.fetch()
        // board 스키마에 postGis 함수들이 없어서 오류가 발생.

        println(result2)
    }

    private fun locationDistance(
        longitude: Double?,
        latitude: Double?,
        distance: Double
    ): BooleanExpression? {
        if (longitude == null || latitude == null) return null

        // ${}로 직접 끼워 넣지 말고, 템플릿의 파라미터 플레이스홀더 {0}, {1}...를 써야 QueryDSL이 타입 바인딩을 제대로 진행한다.
        // 경도, 위도 순서
        val distanceWhere = Expressions.booleanTemplate(
            "function('ST_DWithin', {0}, function('ST_MakePoint', {1}, {2}), {3}) = true",
            board.location,
            longitude,
            latitude,
            distance * 1000
        )
        return distanceWhere
    }
}
