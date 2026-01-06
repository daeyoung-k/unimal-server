package com.unimal.apigateway.config.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MapRouteConfig {

    @Value("\${custom.route.base-uri}")
    private lateinit var mapUri: String

    @Value("\${custom.route.map.port}")
    private lateinit var mapPort: String

    @Bean
    fun mapRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${mapUri}:${mapPort}"
        return builder.routes {
            publicRoutes(baseUri)
        }
    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("mapPublicRoutes") {
            path("/map/**")
            uri(baseUri)
        }
    }
}