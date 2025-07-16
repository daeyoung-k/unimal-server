package com.unimal.apigateway.config.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BoardRouteConfig {

    @Value("\${custom.route.base-uri}")
    private lateinit var boardUri: String

    @Value("\${custom.route.board.port}")
    private lateinit var boardPort: String

    @Bean
    fun boardRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${boardUri}:${boardPort}"
        return builder.routes {
            publicRoutes(baseUri)
        }
    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("board-service") {
            path("/board/**")
            uri(baseUri)
        }
    }
}