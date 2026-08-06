package com.unimal.apigateway.config.routes

/**
 * 모듈별 경로 프리픽스.
 *
 * 게이트웨이는 [org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory]
 * 를 쓰지 않는다. 즉 **프리픽스는 다운스트림까지 그대로 전달되며, 각 서비스의 컨트롤러
 * 매핑도 이 값으로 시작한다.** 게이트웨이에서만 쓰는 접두어가 아니라 서비스 간 약속이다.
 *
 * 상수로 뺀 이유는 오타 방지보다 **누락 방지**에 가깝다. 라우트 파일이 모듈당 하나씩
 * 나뉘어 있어서 경로를 추가할 때 `"/map/feed/section"` 처럼 프리픽스 없이 적어도
 * 컴파일이 통과한다. 그러면 게이트웨이가 라우팅하지 못해 404 가 나는데, 원인이
 * 게이트웨이 설정이라는 걸 알아채기까지 시간이 걸린다
 * (실제로 2026-08-06 `/board/map/feed/section` 에서 한 번 겪었다).
 *
 * `"${'$'}BOARD/post/list"` 형태로 적으면 프리픽스가 눈에 보이는 자리에 고정돼 빠뜨리기 어렵다.
 * `const val` 이라 문자열 템플릿도 컴파일 타임에 접혀 런타임 비용은 없다.
 *
 * 값을 바꿀 일이 생기면 여기만 고쳐서는 안 된다 — 해당 모듈의 컨트롤러 매핑과
 * Flutter 앱의 API 경로도 같이 바뀌어야 한다.
 */
object RoutePrefix {
    const val USER = "/user"
    const val BOARD = "/board"
    const val MAP = "/map"
    const val PHOTO = "/photo"
    const val NOTIFICATION = "/notification"
}
