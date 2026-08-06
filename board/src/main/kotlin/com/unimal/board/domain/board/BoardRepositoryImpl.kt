package com.unimal.board.domain.board

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.unimal.board.controller.post.enums.PostSortType
import com.unimal.board.controller.post.dto.MyPostListRequest
import com.unimal.board.controller.post.dto.PostListRequest
import com.unimal.board.domain.board.like.QBoardLike
import com.unimal.board.domain.board.reply.QBoardReply
import com.unimal.board.enums.PostShow
import org.springframework.stereotype.Repository

@Repository
class BoardRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) {
    private val board = QBoard.board
    private val boardLike = QBoardLike.boardLike
    private val boardFile = QBoardFile.boardFile
    private val boardReply = QBoardReply.boardReply

    fun boardConditionList(
        postListRequest: PostListRequest
    ): List<Board> {
        val conditions = mutableListOf<BooleanExpression>(
            board.show.eq(PostShow.PUBLIC)
        )

        // 내 근처 거리 조건
        if (postListRequest.isLocationSearch) {
            locationDistance(
                postListRequest.longitude,
                postListRequest.latitude,
                postListRequest.distance,
            ).let { conditions += it }
        }

        // 검색어
        postListRequest.keyword?.takeIf { it.isNotBlank() }?.let {
            conditions += keywordCondition(it)
        }

        // 정렬 적용
        val orderBy = when (postListRequest.sortType) {
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
                        .select(boardReply.count())
                        .from(boardReply)
                        .where(boardReply.board.eq(board))
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
            .offset(postListRequest.pageable.offset)
            .limit(postListRequest.pageable.pageSize.toLong())
            .fetch()

        return boards ?: emptyList()
    }

    fun boardMyConditionList(
        email: String,
        myPostListRequest: MyPostListRequest
    ): List<Board> {
        val conditions = mutableListOf<BooleanExpression>(
            board.email.email.eq(email),
        )

        // 검색어
        myPostListRequest.keyword?.takeIf { it.isNotBlank() }?.let {
            conditions += keywordCondition(it)
        }

        // 정렬 적용
        val orderBy = when (myPostListRequest.sortType) {
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
                OrderSpecifier(
                    Order.DESC,
                    JPAExpressions
                        .select(boardReply.count())
                        .from(boardReply)
                        .where(boardReply.board.eq(board))
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
            .offset(myPostListRequest.pageable.offset)
            .limit(myPostListRequest.pageable.pageSize.toLong())
            .fetch()

        return boards ?: emptyList()
    }

    /**
     * 키워드 검색 조건.
     *
     * 제목/내용뿐 아니라 게시글 작성 시점에 이미 저장해 둔 주소 컬럼(도로명, 시도, 구군, 동)까지
     * 함께 본다. "홍대", "강남구" 같은 지역명이 본문에 없어도 그 지역 게시글이 잡히므로
     * 외부 장소검색 API 없이 "지역으로 글 찾기"가 성립한다. 이 컬럼들은 원래 저장만 되고
     * 검색에는 전혀 쓰이지 않던 값이라, 추가 비용 없이 검색 품질만 올라간다.
     *
     * TODO(해시태그): board_hashtag 테이블 추가 시 여기에 태그 조건을 .or() 로 덧붙인다.
     *
     * 주의: containsIgnoreCase 는 LIKE '%kw%' 로 나가 인덱스를 타지 못한다(선행 와일드카드).
     * 게시글이 수만 건 규모가 되면 pg_trgm + GIN 인덱스로 교체해야 한다.
     * ddl-auto 로는 확장/인덱스를 만들 수 없으니 board/migration/ 에 수동 실행 SQL 로 남길 것.
     */
    private fun keywordCondition(keyword: String): BooleanExpression {
        return board.title.containsIgnoreCase(keyword)
            .or(board.content.containsIgnoreCase(keyword))
            .or(board.streetName.containsIgnoreCase(keyword))
            .or(board.siDo.containsIgnoreCase(keyword))
            .or(board.guGun.containsIgnoreCase(keyword))
            .or(board.dong.containsIgnoreCase(keyword))
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
            .orderBy(boardFile.main.desc(), boardFile.id.asc())
            .fetch() ?: emptyList()
    }

    fun boardLikedStoriesList(
        email: String,
        page: Int = 0,
        size: Int = 20
    ): List<Board> {
        return queryFactory
            .selectFrom(board)
            .innerJoin(board.email).fetchJoin()
            .innerJoin(boardLike).on(
                boardLike.board.eq(board),
                boardLike.email.eq(email)
            )
            .where(
                board.del.eq(false),
                board.show.eq(PostShow.PUBLIC),
                board.email.email.ne(email)
            )
            .orderBy(boardLike.createdAt.desc())
            .offset((page * size).toLong())
            .limit(size.toLong())
            .fetch()
    }
}
