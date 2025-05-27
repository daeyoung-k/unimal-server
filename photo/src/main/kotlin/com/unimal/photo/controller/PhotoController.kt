package com.unimal.photo.controller

import com.unimal.common.TestDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotoController {

    @GetMapping("/test")
    fun test(): TestDTO {
        return TestDTO(4)
    }
}