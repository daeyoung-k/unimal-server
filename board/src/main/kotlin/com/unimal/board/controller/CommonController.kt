package com.unimal.board.controller

import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommonController(
    private val hashidsUtil: HashidsUtil,
) {

    @GetMapping("/hashids")
    fun hashidsValue(
        @RequestParam("value") value: Long
    ): CommonResponse {
        return CommonResponse(data = hashidsUtil.encode(value))
    }
}