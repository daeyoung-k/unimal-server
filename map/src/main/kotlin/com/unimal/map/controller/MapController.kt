package com.unimal.map.controller

import com.unimal.common.TestDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MapController {

    @GetMapping("/map")
    fun map() {
        println(TestDTO(2))
    }
}