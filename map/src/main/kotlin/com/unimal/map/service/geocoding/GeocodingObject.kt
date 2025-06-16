package com.unimal.map.service.geocoding

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.unimal.map.service.geocoding.dto.*
import com.unimal.server.webcommon.exception.ApiCallException
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

                val streetNameAddressObject = root["results"].firstOrNull {
                    val types = mapper.treeToValue(it["types"], List::class.java)
                    types.contains("street_address")
                }

                val streetNumberAddressObject = root["results"].firstOrNull {
                    val types = mapper.treeToValue(it["types"], List::class.java)
                    types.contains("point_of_interest")
                }

                val sublocalityObject = root["results"].firstOrNull {
                    val types = mapper.treeToValue(it["types"], List::class.java)
                    types.contains("sublocality") && types.contains("sublocality_level_2")
                }


                val streetNameAddress = mapper.treeToValue(streetNameAddressObject, StreetNameAddress::class.java)
                val streetNumberAddress = mapper.treeToValue(streetNumberAddressObject, StreetNumberAddress::class.java)
                val sublocality: List<Sublocality> = mapper.readValue(sublocalityObject?.get("addressComponents").toString()) ?: emptyList()

                val siDo = sublocality.firstOrNull { it.types.contains("administrative_area_level_1") }?.longText.orEmpty()
                val guGun = sublocality.firstOrNull { it.types.contains("sublocality_level_1") }?.longText.orEmpty()
                val dong = sublocality.firstOrNull { it.types.contains("sublocality_level_2") }?.longText.orEmpty()

                AddressResult(
                    streetNameAddress.formattedAddress.replace("대한민국", "").trim(),
                    streetNumberAddress.formattedAddress.replace("대한민국", "").trim(),
                    streetNameAddress.postalAddress.postalCode,
                    siDo = siDo,
                    guGun = guGun,
                    dong = dong,
                )
            } else {
                AddressResult()
            }

        } catch (e: Exception) {
            // Handle the exception, e.g., log it or rethrow it
            throw ApiCallException("Geocoding API 호출 중 오류가 발생했습니다: ${e.message}")
        }

    }
}