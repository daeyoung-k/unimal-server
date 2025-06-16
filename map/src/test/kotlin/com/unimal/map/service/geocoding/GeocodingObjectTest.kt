package com.unimal.map.service.geocoding

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.client.RestTemplate
import kotlin.jvm.java

@SpringBootTest
@ActiveProfiles("local")
class GeocodingObjectTest {

    private val geocodingJson = geocodingTestJson
    private val latitude = "37.541003"
    private val longitude = "127.086777"
    private val geocodingApiKey = "테스트_api_key"
    private val mapper = jacksonObjectMapper()

    private lateinit var restTemplate: RestTemplate
    private lateinit var geocodingObject: GeocodingObject


    @BeforeEach
    fun setUp() {
        restTemplate = mockk(relaxed = true)
        geocodingObject = GeocodingObject(geocodingApiKey, restTemplate)
    }

    @AfterEach
    fun tearDown() = unmockkAll()

    @Test
    fun `getAddress - 주소를 정상적으로 조회한다`() {
        //Given
        val resultBody = geocodingJson

        val url = "https://geocode.googleapis.com/v4beta/geocode/location/$latitude,$longitude?key=$geocodingApiKey"

        every {
            restTemplate.getForObject(
                url,
                String::class.java
            )
        } returns resultBody

        //When
        val result = geocodingObject.getAddress(
            latitude.toDouble(),
            longitude.toDouble()
        )

        // Then
        assertNotNull(result)
        assertTrue(result.streetName.isNotBlank() || result.streetNumber.isNotBlank())
    }

    @Test
    fun `getAddressObject - 도로주소 객체를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]

        //When
        val result = geocodingObject.getAddressObject(rootResults, mapper, "street_address")

        //Then
        assertNotNull(result)
        assertTrue(result is JsonNode)
    }

    @Test
    fun `getAddressObject - 도로주소 객체를 서브타입을 활용하여 정상적으로 조회한다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]

        //When
        val result = geocodingObject.getAddressObject(rootResults, mapper, "sublocality", "sublocality_level_2")

        //Then
        assertNotNull(result)
        assertTrue(result is JsonNode)
    }

    @Test
    fun `getAddressObject - 도로주소 객체를 조회하지 못하여 null 을 반환한다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]

        //When
        val result = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")

        //Then
        assertNull(result)
    }

    @Test
    fun `getStreetAddress - 도로주소를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "street_address")

        //When
        val result = geocodingObject.getStreetAddress(streetObject, mapper)

        //Then
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getStreetAddress - 도로주소를 가져오지 못하면 공백을 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")

        //When
        val result = geocodingObject.getStreetAddress(streetObject, mapper)

        //Then
        assertNotNull(result)
        assertTrue(result.isBlank())
    }

    @Test
    fun `getLongShortText - 시, 도, 동 주소를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val sublocalityObject = geocodingObject.getAddressObject(rootResults, mapper, "sublocality", "sublocality_level_2")

        //When
        val result = geocodingObject.getLongShortText(sublocalityObject, mapper, "administrative_area_level_1")

        //Then
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getLongShortText - 시, 도, 동 주소를 가져오지 못하면 공백을 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val sublocalityObject = geocodingObject.getAddressObject(rootResults, mapper, "sublocality", "sublocality_level_2")

        //When
        val result = geocodingObject.getLongShortText(sublocalityObject, mapper, "없는_타입")

        //Then
        assertNotNull(result)
        assertTrue(result.isBlank())
    }

    @Test
    fun `getLongShortText - 우편번호를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "street_address")
        val postalCodeObject = geocodingObject.getAddressObject(rootResults, mapper, "postal_code")

        //When
        val result = geocodingObject.getLongShortText(streetObject, mapper, "postal_code", postalCodeObject)

        //Then
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getLongShortText - 도로주소 객체가 없어도 우편번호를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")
        val postalCodeObject = geocodingObject.getAddressObject(rootResults, mapper, "postal_code")

        //When
        val result = geocodingObject.getLongShortText(streetObject, mapper, "postal_code", postalCodeObject)

        //Then
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getLongShortText - 우편번호 객체가 없어도 우편번호를 정상적으로 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "street_address")
        val postalCodeObject = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")

        //When
        val result = geocodingObject.getLongShortText(streetObject, mapper, "postal_code", postalCodeObject)

        //Then
        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getLongShortText - 도로주소, 우편번호 객체가 없으면 공백을 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")
        val postalCodeObject = geocodingObject.getAddressObject(rootResults, mapper, "없는_타입")

        //When
        val result = geocodingObject.getLongShortText(streetObject, mapper, "postal_code", postalCodeObject)

        //Then
        assertNotNull(result)
        assertTrue(result.isBlank())
    }

    @Test
    fun `getLongShortText - 우편번호를 가져오지 못하면 공백을 가져온다`() {
        //Given
        val root = mapper.readTree(geocodingJson)
        val rootResults = root["results"]
        val streetObject = geocodingObject.getAddressObject(rootResults, mapper, "street_address")
        val postalCodeObject = geocodingObject.getAddressObject(rootResults, mapper, "postal_code")

        //When
        val result = geocodingObject.getLongShortText(streetObject, mapper, "없는_타입", postalCodeObject)

        //Then
        assertNotNull(result)
        assertTrue(result.isBlank())
    }

}