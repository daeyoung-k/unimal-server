package com.unimal.apigateway.config.routes

import com.unimal.apigateway.config.routes.RoutePrefix.BOARD
import com.unimal.apigateway.config.routes.filter.OptionalAccessTokenFilter
import com.unimal.apigateway.config.routes.filter.AccessTokenFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BoardRouteConfig(
    private val accessTokenFilter: AccessTokenFilter,
    private val optionalAccessTokenFilter: OptionalAccessTokenFilter,
) {

    @Value("\${custom.route.board.uri}")
    private lateinit var boardUri: String

    @Value("\${custom.route.board.port}")
    private lateinit var boardPort: String

    @Bean
    fun boardRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${boardUri}:${boardPort}"
        return builder.routes {
            publicRoutes(baseUri)
            filterRoutes(baseUri)
        }
    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("boardPublicRoutes") {
            path(
                "$BOARD/hashids",
                "$BOARD/hashids/**",
                "$BOARD/notice",
                "$BOARD/notice/**",
            )
            uri(baseUri)
        }

    }

    private fun RouteLocatorDsl.filterRoutes(
        baseUri: String
    ) {
        route("boardPostOptionalAccessTokenFilterRoutes") {
            path(
                "$BOARD/post/list",
                "$BOARD/post/{boardId}",

                // 지도 바텀카드 피드 — 비로그인 허용이므로 optional 쪽에 둔다.
                // 필수 필터 쪽에 넣으면 비로그인 진입이 401 로 막힌다.
                "$BOARD/map/feed",
                // 섹션 단위 새로고침(헤더 ↻ 버튼). PathPattern 은 하위 경로를
                // 자동으로 먹지 않아 별도 등록이 필요하다 — 이게 없으면 게이트웨이가
                // 라우팅하지 못하고, 앱은 실패를 조용히 삼켜(MapFeedSectionResult.failed)
                // "버튼을 눌러도 아무 일도 안 일어나는" 상태가 된다. (2026-08-06 수정)
                "$BOARD/map/feed/section",
            )
                .filters { f ->
                    f.filter(optionalAccessTokenFilter.apply(OptionalAccessTokenFilter.Config()))
                }
            uri(baseUri)
        }

        route("boardPostAccessTokenFilterRoutes") {
            path(
                // etc
                "$BOARD/post/total",
                "$BOARD/post/total/like",
                "$BOARD/post/like/stories/total",

                // 게시판 관련
                "$BOARD/post",
                "$BOARD/post/my/list",
                "$BOARD/post/{boardId}/like",
                "$BOARD/post/{boardId}/delete",
                "$BOARD/post/{boardId}/update",
                "$BOARD/post/{boardId}/file/upload",
                "$BOARD/post/{boardId}/file/delete",
                "$BOARD/post/{boardId}/reply",
                "$BOARD/post/{boardId}/reply/{replyId}/update",
                "$BOARD/post/{boardId}/reply/{replyId}/delete",

                // 지도 관련
                "$BOARD/map/location/post",
                "$BOARD/map/post",

                // 신고 — 인증 필수. 누가 신고했는지 남겨야 중복 신고를 막을 수 있고,
                // 익명 신고를 허용하면 도배로 특정 글을 묻어버릴 수 있다.
                //
                // board 에 컨트롤러가 구현돼 있었는데 여기 등록이 빠져 있어서
                // 앱에서 호출하면 404 가 났다 (2026-08-07 발견).
                "$BOARD/report",
            )
            .filters { f ->
                f.filter(accessTokenFilter.apply(AccessTokenFilter.Config()))
            }
            uri(baseUri)
        }

    }
}