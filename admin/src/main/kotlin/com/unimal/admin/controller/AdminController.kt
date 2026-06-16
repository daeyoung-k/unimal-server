package com.unimal.admin.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.stereotype.Controller

@Controller
class AdminController {

    @GetMapping
    fun index(): String {
        return "redirect:/members"
    }
}
