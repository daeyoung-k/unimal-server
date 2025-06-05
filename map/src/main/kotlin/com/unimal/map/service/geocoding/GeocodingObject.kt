package com.unimal.map.service.geocoding

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.map.service.geocoding.dto.AddressResult
import com.unimal.map.service.geocoding.dto.StreetNameAddress
import com.unimal.map.service.geocoding.dto.StreetNumberAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GeocodingObject(
    @Value("\${custom.data.map.api-key}")
    private val geocodingApiKey: String
) {

    fun getAddress(
        latitude: Double,
        longitude: Double
    ): AddressResult {
        val restTemplate = RestTemplate()
        val url = "https://geocode.googleapis.com/v4beta/geocode/location/$latitude,$longitude?key=$geocodingApiKey"
        try {
            val response = restTemplate.getForObject(url, String::class.java)

            val mapper = jacksonObjectMapper()
            val root = mapper.readTree(response)
            return if (root["results"].isArray) {
                val streetNameAddress = mapper.treeToValue(root["results"][0], StreetNameAddress::class.java)
                val streetNumberAddress = mapper.treeToValue(root["results"][1], StreetNumberAddress::class.java)
                AddressResult(
                    streetNameAddress.formattedAddress,
                    streetNumberAddress.formattedAddress,
                    streetNameAddress.postalAddress.postalCode,
                )
            } else {
                AddressResult()
            }

        } catch (e: Exception) {
            // Handle the exception, e.g., log it or rethrow it
            throw RuntimeException("Failed to get address for coordinates: $latitude, $longitude", e)
        }

    }
}