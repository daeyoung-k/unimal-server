package com.unimal.user.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class UserController(
) {

    @GetMapping("/test")
    fun test(
        request: HttpServletRequest
    ) {
        println(request)
        println("testestsetse")

    }
}