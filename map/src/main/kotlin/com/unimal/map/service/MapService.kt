package com.unimal.map.service

import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.service.geocoding.GeocodingObject
import com.unimal.map.service.geocoding.dto.AddressResult
import org.springframework.stereotype.Service

@Service
class MapService(
    private val geocodingObject: GeocodingObject
) {

    fun reverseGeocoding(latLngRequest: LatLngRequest): AddressResult {
        return geocodingObject.getAddress(
            latLngRequest.latitude,
            latLngRequest.longitude
        )
    }
}