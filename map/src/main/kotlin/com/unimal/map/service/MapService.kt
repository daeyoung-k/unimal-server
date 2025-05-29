package com.unimal.map.service

import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.service.geocoding.GeocodingObject
import org.springframework.stereotype.Service

@Service
class MapService(
    private val geocodingObject: GeocodingObject
) {

    fun reverseGeocoding(latLngRequest: LatLngRequest) {
        geocodingObject.getAddress(
            latLngRequest.latitude,
            latLngRequest.longitude
        )


    }
}