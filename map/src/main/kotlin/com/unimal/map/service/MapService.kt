package com.unimal.map.service

import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.service.geocoding.GeocodingObject
import com.unimal.map.service.geocoding.dto.ReverseGeocodingResult
import org.springframework.stereotype.Service

@Service
class MapService(
    private val geocodingObject: GeocodingObject
) {

    fun reverseGeocoding(latLngRequest: LatLngRequest): ReverseGeocodingResult {
        return geocodingObject.getAddress(
            latLngRequest.latitude,
            latLngRequest.longitude
        )
    }
}