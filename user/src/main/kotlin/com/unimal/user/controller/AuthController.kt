package com.unimal.user.controller

import com.unimal.user.config.annotation.UserInfoAnnotation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController {

    @GetMapping("/access-token")
    fun accessToken(
        @UserInfoAnnotation refreshToken: String,
    ) {

        println(refreshToken)

    }
}