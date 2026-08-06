package com.unimal.apigateway.config.routes

import com.unimal.apigateway.config.routes.RoutePrefix.BOARD
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 게시글 공유 페이지 라우팅.
 *
 * 설계: `docs/specs/2026-08-07-게시글-공유.md`
 *
 * ## 이 저장소에서 유일한 RewritePath 다
 *
 * 다른 모듈은 게이트웨이 경로와 서비스 `context-path` 가 같아서 프리픽스를 그대로
 * 넘긴다([RoutePrefix] KDoc). 공유만 다르다.
 *
 * ```
 * 외부: https://stomap.unimal.co.kr/s/aBc123
 * 내부: http://board:8083/board/s/aBc123
 * ```
 *
 * 이유는 순전히 **URL 길이**다. 공유 링크는 카톡·문자에 날것으로 보이고 QR·인쇄물에도
 * 들어간다. `/board/s/aBc123` 의 `/board` 는 사용자에게 아무 의미가 없는데 자리만
 * 차지한다. 짧은 주소를 위해 규칙의 예외를 하나 감수한 것이다.
 *
 * **[BoardRouteConfig] 가 아니라 파일을 따로 뒀다.** 같은 board 로 가지만 규칙이
 * 다르기 때문이다. 섞어두면 "board 라우트는 프리픽스를 그대로 넘긴다"는 설명이
 * 반만 맞는 말이 되고, 다음 사람이 여기를 보고 다른 경로도 rewrite 해도 되는 줄 안다.
 *
 * ## 인증 필터가 없다
 *
 * 공유 페이지는 **비로그인 + 앱 미설치 상태에서 열리는 것이 정상**이다.
 * 카카오·슬랙 크롤러도 비로그인으로 온다. 필터를 붙이면 미리보기 카드가 통째로 죽는다.
 */
@Configuration
class ShareRouteConfig {

    @Value("\${custom.route.board.uri}")
    private lateinit var boardUri: String

    @Value("\${custom.route.board.port}")
    private lateinit var boardPort: String

    @Bean
    fun shareRouting(builder: RouteLocatorBuilder): RouteLocator {
        val baseUri = "${boardUri}:${boardPort}"
        return builder.routes {
            route("sharePublicRoutes") {
                path("$SHARE_PREFIX/**")
                    .filters { f ->
                        // (?<id>.*) 로 캡처해 board 의 context-path 를 앞에 붙인다.
                        // Kotlin 문자열이라 '\$' 로 이스케이프해야 치환 참조 "${'$'}{id}" 로 넘어간다.
                        f.rewritePath("$SHARE_PREFIX/(?<id>.*)", "$BOARD$SHARE_PREFIX/\${id}")
                    }
                uri(baseUri)
            }
        }
    }

    companion object {
        /**
         * 공유 경로. 짧을수록 좋다 — 카톡에 그대로 보이는 주소다.
         *
         * **[RoutePrefix] 에 넣지 않았다.** 저기는 "모듈 = 프리픽스" 목록인데 `/s` 는
         * 모듈이 아니라 board 안의 한 기능이다. 섞으면 목록의 의미가 흐려진다.
         */
        private const val SHARE_PREFIX = "/s"
    }
}
