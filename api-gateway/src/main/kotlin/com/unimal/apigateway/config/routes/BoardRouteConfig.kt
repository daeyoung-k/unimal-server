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

    @Value("\${custom.route.base-uri}")
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

    }

    private fun RouteLocatorDsl.filterRoutes(
        baseUri: String
    ) {
        route("boardPostOptionalAccessTokenFilterRoutes") {
            path(
                "/board/post/list",
                "/board/post/{boardId}",
                "/board/post/{boardId}/like"
            )
                .filters { f ->
                    f.filter(optionalAccessTokenFilter.apply(OptionalAccessTokenFilter.Config()))
                }
            uri(baseUri)
        }

        route("boardPostAccessTokenFilterRoutes") {
            path(
                "/board/post"
            )
            .filters { f ->
                f.filter(accessTokenFilter.apply(AccessTokenFilter.Config()))
            }
            uri(baseUri)
        }

    }
}