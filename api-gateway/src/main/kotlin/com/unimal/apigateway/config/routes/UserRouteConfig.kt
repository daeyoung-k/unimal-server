package com.unimal.apigateway.config.routes

import com.unimal.apigateway.config.routes.filter.RefreshTokenFilter
import com.unimal.apigateway.config.routes.filter.AccessTokenFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserRouteConfig(
    private val accessTokenFilter: AccessTokenFilter,
    private val refreshTokenFilter: RefreshTokenFilter,
) {

    @Value("\${custom.route.base-uri}")
    private lateinit var userUri: String

    @Value("\${custom.route.user.port}")
    private lateinit var userPort: String

    /**
     * builder 방식을 사용하면 하나의 함수에 모두 포함시켜야 함으로 공용 및 필터 API를 분리해서 관리하기 어려움이 있다.
     * 따라서, RouteLocatorDsl을 사용하여 공용 API와 필터 API를 분리하여 관리한다.
     * 도메인 별로 나눠서 관리하는 것도 용이하다.
     */
    @Bean
    fun userRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${userUri}:${userPort}"
        return builder.routes {
            publicRoutes(baseUri)
            filterRoutes(baseUri)
        }

    }

    private fun RouteLocatorDsl.publicRoutes(
        baseUri: String
    ) {
        route("userAuthPublicRoutes") {
            path(
                "/user/auth/login/**",
                "/user/auth/signup/**",
                "/user/auth/email/**",
                "/user/auth/tel/**",
                "/user/auth/email-tel/**"
            )
            uri(baseUri)
        }
        route("userMemberPublicRoutes") {
            path("/user/member/find/**")
            uri(baseUri)
        }
        route("userSlangPublicRoutes") {
            path("/user/slang/**")
            uri(baseUri)
        }
    }

    private fun RouteLocatorDsl.filterRoutes(
        baseUri: String
    ) {
        route("userAuthRefreshTokenFilterRoutes") {
            path(
                "/user/auth/token-reissue",
                "/user/auth/logout",
                "/user/auth/withdrawal",
            )
            .filters { f ->
                f.filter(refreshTokenFilter.apply(RefreshTokenFilter.Config()))
            }
            uri(baseUri)
        }

        route("userMember-onlyAccessTokenFilterRoutes") {
            path(
                "/user/member/info/**",
                "/user/member/change/password",
            )
            .filters { f ->
                f.filter(accessTokenFilter.apply(AccessTokenFilter.Config()))
            }
            uri(baseUri)
        }
    }
}
