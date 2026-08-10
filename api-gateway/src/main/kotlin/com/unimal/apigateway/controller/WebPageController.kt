package com.unimal.apigateway.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
class WebPageController {

    /**
     * 서비스 랜딩 페이지 — `https://stomap.unimal.co.kr/`
     *
     * 설계: `docs/specs/2026-08-10-랜딩페이지.md`
     *
     * 루트 경로인 이유, 스토어 리다이렉트를 쓰지 않는 이유는 위 문서에 있다.
     * 이 파일만 캐싱하는 이유는 유입 전면이라 크롤러가 반복 호출하기 때문이다.
     */
    private val landingHtml: String by lazy {
        ClassPathResource("static/landing.html").inputStream.readBytes().toString(Charsets.UTF_8)
    }

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun landing(): Mono<String> = Mono.just(landingHtml)

    @GetMapping("/stomap/privacy", produces = [MediaType.TEXT_HTML_VALUE])
    fun privacy(): Mono<String> {
        val html = ClassPathResource("static/privacy.html").inputStream.readBytes().toString(Charsets.UTF_8)
        return Mono.just(html)
    }

    @GetMapping("/stomap/delete-account", produces = [MediaType.TEXT_HTML_VALUE])
    fun deleteAccount(): Mono<String> {
        val html = ClassPathResource("static/delete-account.html").inputStream.readBytes().toString(Charsets.UTF_8)
        return Mono.just(html)
    }

    @GetMapping("/stomap/support", produces = [MediaType.TEXT_HTML_VALUE])
    fun support(): Mono<String> {
        val html = ClassPathResource("static/support.html").inputStream.readBytes().toString(Charsets.UTF_8)
        return Mono.just(html)
    }

    @GetMapping("/stomap/terms", produces = [MediaType.TEXT_HTML_VALUE])
    fun terms(): Mono<String> {
        val html = ClassPathResource("static/terms.html").inputStream.readBytes().toString(Charsets.UTF_8)
        return Mono.just(html)
    }

    /**
     * AdMob app-ads.txt — 광고 판매자 인증용(애드 사기 방지).
     * 반드시 도메인 "루트"(/app-ads.txt)로 노출돼야 구글이 크롤링한다. (/stomap 하위 불가)
     * 스토어 등록정보의 개발자 웹사이트 도메인과 일치해야 효력 발생.
     */
    @GetMapping("/app-ads.txt", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun appAdsTxt(): Mono<String> {
        val txt = ClassPathResource("static/app-ads.txt").inputStream.readBytes().toString(Charsets.UTF_8)
        return Mono.just(txt)
    }
}