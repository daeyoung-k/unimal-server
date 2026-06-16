package com.unimal.admin.controller.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginViewController {

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }
}
