package com.unimal.map.controller

import com.unimal.common.dto.CommonResponse
import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.service.MapService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RestController

@RestController
class MapController(
    private val mapService: MapService,
) {

    @GetMapping("/reverse-geocoding")
    fun reverseGeocoding(
        @ModelAttribute @Valid latLngRequest: LatLngRequest
    ): CommonResponse {
        return CommonResponse(data = mapService.reverseGeocoding(latLngRequest))
    }
}