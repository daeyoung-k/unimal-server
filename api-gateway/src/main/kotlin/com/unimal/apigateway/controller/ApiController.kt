package com.unimal.apigateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {

    @GetMapping("/test")
    fun test() {
        println("test")
    }
}