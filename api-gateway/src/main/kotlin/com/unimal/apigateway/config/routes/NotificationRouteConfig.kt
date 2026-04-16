package com.unimal.apigateway.config.routes

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationRouteConfig {

    @Value("\${custom.route.notification.uri}")
    private lateinit var notificationUri: String

    @Value("\${custom.route.notification.port}")
    private lateinit var notificationPort: String

    @Bean
    fun notificationRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${notificationUri}:${notificationPort}"
        return builder.routes {
            publicRoutes(baseUri)
        }
    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("notificationPublicRoutes") {
            path(
                "/notification/app-push",
            )
            uri(baseUri)
        }
    }
}