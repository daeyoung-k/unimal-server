package com.unimal.board.domain.board

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.unimal.board.controller.enums.PostSortType
import com.unimal.board.controller.request.PostsListRequest
import com.unimal.board.domain.board.like.QBoardLike
import com.unimal.board.enums.PostShow
import org.springframework.stereotype.Repository

@Repository
class BoardRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) {
    private val board = QBoard.board
    private val boardLike = QBoardLike.boardLike
    private val boardFile = QBoardFile.boardFile

    fun boardConditionList(
        postsListRequest: PostsListRequest
    ): List<Board> {
        val conditions = mutableListOf<BooleanExpression>(
            board.show.eq(PostShow.PUBLIC)
        )

        // 내 근처 거리 조건
        if (postsListRequest.isLocationSearch) {
            locationDistance(
                postsListRequest.longitude,
                postsListRequest.latitude,
                postsListRequest.distance,
            ).let { conditions += it }
        }

        // 검색어
        postsListRequest.keyword?.let {
            conditions += board.title.containsIgnoreCase(it).or( board.content.containsIgnoreCase(it) )
        }

        // 정렬 적용
        val orderBy = when (postsListRequest.sortType) {
            PostSortType.LIKES -> {
                OrderSpecifier(
                    Order.DESC,
                    JPAExpressions
                        .select(boardLike.count())
                        .from(boardLike)
                        .where(boardLike.board.eq(board))
                )
            }
            PostSortType.REPLYS -> {
                // 댓글 기능 생성시 적용 예정
                OrderSpecifier(
                    Order.DESC,
                    JPAExpressions
                        .select(boardLike.count())
                        .from(boardLike)
                        .where(boardLike.board.eq(board))
                )
            }
            else -> board.createdAt.desc()
        }

        // 게시글 조회
        val boards = queryFactory
            .selectFrom(board)
            .where(
                board.del.eq(false),
                *conditions.toTypedArray()
            )
            .orderBy(orderBy)
            .offset(postsListRequest.pageable.offset)
            .limit(postsListRequest.pageable.pageSize.toLong())
            .fetch()

        return boards ?: emptyList()
    }

    private fun locationDistance(
        longitude: Double?,
        latitude: Double?,
        distance: Double
    ): BooleanExpression {

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

    fun boardFileList(
        idList: List<Long>
    ): List<BoardFile> {
        return queryFactory
            .selectFrom(boardFile)
            .where(
                boardFile.board.id.`in`(idList)
            )
            .fetch() ?: emptyList()
    }
}
