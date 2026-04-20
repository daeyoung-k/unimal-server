package com.unimal.apigateway.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
class WebPageController {

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
}