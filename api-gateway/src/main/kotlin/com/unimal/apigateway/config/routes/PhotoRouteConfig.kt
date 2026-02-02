package com.unimal.apigateway.config.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PhotoRouteConfig {

    @Value("\${custom.route.photo.uri}")
    private lateinit var photoUri: String

    @Value("\${custom.route.photo.port}")
    private lateinit var photoPort: String

    @Bean
    fun photoRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${photoUri}:${photoPort}"
        return builder.routes {
            publicRoutes(baseUri)
        }

    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("photoPublicRoutes") {
            path("/photo/**")
            uri(baseUri)
        }
    }
}