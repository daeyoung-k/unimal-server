package com.unimal.map.service.geocoding.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StreetNameAddress(
    val formattedAddress: String = "",
    val postalAddress: PostalCode = PostalCode(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StreetNumberAddress(
    val formattedAddress: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostalCode(
    val postalCode: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sublocality(
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