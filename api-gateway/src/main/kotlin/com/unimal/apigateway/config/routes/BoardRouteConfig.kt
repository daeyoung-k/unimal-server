package com.unimal.apigateway.config.routes

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
                "/board/hashids",
                "/board/hashids/**",
                "/board/notice",
                "/board/notice/**",
            )
            uri(baseUri)
        }

    }

    private fun RouteLocatorDsl.filterRoutes(
        baseUri: String
    ) {
        route("boardPostOptionalAccessTokenFilterRoutes") {
            path(
                "/board/post/list",
                "/board/post/{boardId}",

                // 지도 바텀카드 피드 — 비로그인 허용이므로 optional 쪽에 둔다.
                // 필수 필터 쪽에 넣으면 비로그인 진입이 401 로 막힌다.
                "/board/map/feed",
                // 섹션 단위 새로고침(헤더 ↻ 버튼). PathPattern 은 하위 경로를
                // 자동으로 먹지 않아 별도 등록이 필요하다 — 이게 없으면 게이트웨이가
                // 라우팅하지 못하고, 앱은 실패를 조용히 삼켜(MapFeedSectionResult.failed)
                // "버튼을 눌러도 아무 일도 안 일어나는" 상태가 된다. (2026-08-06 수정)
                "/board/map/feed/section",
            )
                .filters { f ->
                    f.filter(optionalAccessTokenFilter.apply(OptionalAccessTokenFilter.Config()))
                }
            uri(baseUri)
        }

        route("boardPostAccessTokenFilterRoutes") {
            path(
                // etc
                "/post/total",
                "/post/total/like",
                "/post/like/stories/total",

                // 게시판 관련
                "/board/post",
                "/board/post/my/list",
                "/board/post/{boardId}/like",
                "/board/post/{boardId}/delete",
                "/board/post/{boardId}/update",
                "/board/post/{boardId}/file/upload",
                "/board/post/{boardId}/file/delete",
                "/board/post/{boardId}/reply",
                "/board/post/{boardId}/reply/{replyId}/update",
                "/board/post/{boardId}/reply/{replyId}/delete",

                // 지도 관련
                "/board/map/location/post",
                "/board/map/post",
            )
            .filters { f ->
                f.filter(accessTokenFilter.apply(AccessTokenFilter.Config()))
            }
            uri(baseUri)
        }

    }
}