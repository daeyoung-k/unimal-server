package com.unimal.map.service

import com.unimal.map.controller.request.LatLngRequest
import com.unimal.map.controller.request.PlaceSearchRequest
import com.unimal.map.service.geocoding.GeocodingObject
import com.unimal.map.service.geocoding.dto.AddressResult
import com.unimal.map.service.search.PlaceSearchService
import com.unimal.map.service.search.dto.PlaceInfo
import org.springframework.stereotype.Service

@Service
class MapService(
    private val geocodingObject: GeocodingObject,
    private val placeSearchService: PlaceSearchService
) {

    fun reverseGeocoding(latLngRequest: LatLngRequest): AddressResult {
        return geocodingObject.getAddress(
            latLngRequest.latitude,
            latLngRequest.longitude
        )
    }

    fun searchPlace(placeSearchRequest: PlaceSearchRequest): List<PlaceInfo> {
        return placeSearchService.search(placeSearchRequest.query)
    }
}
