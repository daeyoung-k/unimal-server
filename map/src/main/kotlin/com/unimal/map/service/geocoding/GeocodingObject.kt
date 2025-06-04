package com.unimal.map.service.geocoding

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.unimal.map.service.geocoding.dto.ReverseGeocodingApiResult
import com.unimal.map.service.geocoding.dto.ReverseGeocodingResult
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
    ): ReverseGeocodingResult {
        val restTemplate = RestTemplate()
        val url = "https://geocode.googleapis.com/v4beta/geocode/location/$latitude,$longitude?key=$geocodingApiKey"
        try {
            val response = restTemplate.getForObject(url, String::class.java)

            val mapper = jacksonObjectMapper()
            val root = mapper.readTree(response)
            return if (root["results"].isArray) {
                val apiResult = mapper.treeToValue(root["results"][0], ReverseGeocodingApiResult::class.java)
                apiResult.toResult()
            } else {
                ReverseGeocodingApiResult().toResult()
            }

        } catch (e: Exception) {
            // Handle the exception, e.g., log it or rethrow it
            throw RuntimeException("Failed to get address for coordinates: $latitude, $longitude", e)
        }

    }
}