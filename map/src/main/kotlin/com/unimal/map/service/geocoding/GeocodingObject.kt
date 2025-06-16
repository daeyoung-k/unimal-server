package com.unimal.map.service.geocoding

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.unimal.map.service.geocoding.dto.*
import com.unimal.webcommon.exception.ApiCallException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import kotlin.collections.firstOrNull

@Component
class GeocodingObject(
    @Value("\${custom.data.map.api-key}")
    private val geocodingApiKey: String,
    private val restTemplate: RestTemplate
) {

    fun getAddress(
        latitude: Double,
        longitude: Double
    ): AddressResult {
        val url = "https://geocode.googleapis.com/v4beta/geocode/location/$latitude,$longitude?key=$geocodingApiKey"
        try {
            val response = restTemplate.getForObject(url, String::class.java)

            val mapper = jacksonObjectMapper()
            val root = mapper.readTree(response)
            return if (root["results"].isArray) {

                val streetNameAddressObject = getAddressObject(root["results"], mapper, "street_address")
                val streetNumberAddressObject = getAddressObject(root["results"], mapper, "point_of_interest")
                val sublocalityObject = getAddressObject(root["results"], mapper, "sublocality", "sublocality_level_2")
                val postalCodeObject = getAddressObject(root["results"], mapper, "postal_code")

                AddressResult(
                    streetName = getStreetAddress(streetNameAddressObject, mapper),
                    streetNumber = getStreetAddress(streetNumberAddressObject, mapper),
                    postalCode = getLongShortText(streetNameAddressObject, mapper, "postal_code", postalCodeObject),
                    siDo = getLongShortText(sublocalityObject, mapper, "administrative_area_level_1"),
                    guGun = getLongShortText(sublocalityObject, mapper, "sublocality_level_1"),
                    dong = getLongShortText(sublocalityObject, mapper, "sublocality_level_2"),
                )
            } else {
                AddressResult()
            }

        } catch (e: Exception) {
            throw ApiCallException("Geocoding API 호출 중 오류가 발생했습니다: ${e.message}")
        }

    }


    fun getAddressObject(
        results: JsonNode,
        mapper: ObjectMapper,
        type: String,
        subType: String? = null
    ): JsonNode? {

        return if (subType.isNullOrBlank()) {
            results.firstOrNull {
                val types = mapper.treeToValue(it["types"], List::class.java)
                types.contains(type)
            }
        } else {
            results.firstOrNull {
                val types = mapper.treeToValue(it["types"], List::class.java)
                types.contains(type) && types.contains(subType)
            }
        }
    }

    fun getStreetAddress(
        objects: JsonNode?,
        mapper: ObjectMapper,
    ): String {
        return if (objects == null) {
            ""
        } else {
            val streetAddress = mapper.treeToValue(objects, StreetAddress::class.java)
            streetAddress.formattedAddress.replace("대한민국", "").trim()
        }
    }

    fun getLongShortText(
        objects: JsonNode?,
        mapper: ObjectMapper,
        type: String,
        subObjects: JsonNode? = null,
    ): String {
        val longShortText: List<LongShortText> = mapper.readValue(objects?.get("addressComponents").toString()) ?: emptyList()
        val result = longShortText.firstOrNull { it.types.contains(type) }?.longText.orEmpty()
        if (result.isNotBlank()) return result

        return if (type == "postal_code" && subObjects != null) {
            val subLongShortText: List<LongShortText> = mapper.readValue(subObjects.get("addressComponents").toString()) ?: return ""
            subLongShortText.firstOrNull { it.types.contains(type) }?.longText.orEmpty()
        } else {
            ""
        }
    }
}