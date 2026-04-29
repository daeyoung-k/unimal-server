package com.unimal.admin.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController {

    @GetMapping
    fun adminHelloWorld(): String {
        return "Hello World!!"
    }
}