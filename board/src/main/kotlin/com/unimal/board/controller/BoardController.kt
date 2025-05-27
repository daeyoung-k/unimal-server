package com.unimal.board.controller

import com.unimal.common.TestDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardController {

    @GetMapping("/test")
    fun test(): TestDTO {

        return TestDTO(3)
    }
}