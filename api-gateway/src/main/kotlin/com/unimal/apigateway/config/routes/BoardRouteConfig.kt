package com.unimal.apigateway.config.routes

import com.unimal.apigateway.filter.TokenFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BoardRouteConfig(
    private val tokenFilter: TokenFilter,
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
        route("board-public-posts") {
            path(
                "/board/posts/list"
            )
            uri(baseUri)
        }
    }

    private fun RouteLocatorDsl.filterRoutes(
        baseUri: String
    ) {
        route("board-private-posts") {
            path(
                "/board/posts"
            )
            .filters { f ->
                f.filter(tokenFilter.apply(TokenFilter.Config()))
            }
            uri(baseUri)
        }

    }
}