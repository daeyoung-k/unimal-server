package com.unimal.map.service.geocoding.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StreetAddress(
    val formattedAddress: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LongShortText(
    val longText: String = "",
    val shortText: String = "",
    val types: List<String> = emptyList(),
)

data class AddressResult(
    val streetName: String = "",
    val streetNumber: String = "",
    val postalCode: String = "",
    val siDo: String = "",
    val guGun: String = "",
    val dong: String = "",
)