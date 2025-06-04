package com.unimal.map.service.geocoding.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReverseGeocodingApiResult(
    val formattedAddress: String = "",
    val postalAddress: PostalCode = PostalCode(),
) {
    fun toResult(): ReverseGeocodingResult {
        return ReverseGeocodingResult(
            address = formattedAddress,
            postalCode = postalAddress.postalCode
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostalCode(
    val postalCode: String = "",
)

data class ReverseGeocodingResult(
    val address: String = "",
    val postalCode: String = "",
)