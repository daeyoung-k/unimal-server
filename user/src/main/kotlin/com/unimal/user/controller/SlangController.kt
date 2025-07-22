package com.unimal.user.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.user.controller.request.ProfanityRequest
import com.unimal.user.service.slang.SlangService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/slang")
class SlangController(
    private val slangService: SlangService
) {

    @PostMapping("/profanity/insert")
    fun insertProfanity(
        @RequestBody @Valid profanityRequest: ProfanityRequest
    ): CommonResponse {
        slangService.insertProfanity(profanityRequest.profanity)
        return CommonResponse()
    }
}