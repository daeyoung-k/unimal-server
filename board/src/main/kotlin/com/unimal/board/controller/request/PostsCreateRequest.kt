package com.unimal.board.controller.request

data class PostsCreateRequest(
    val title: String,
    val content: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val streetName: String? = null,
    val postalCode: String? = null,
    val siDo: String? = null,
    val guGun: String? = null,
    val dong: String? = null,
)

data class ImageMetadata(
    val order: Int,
    val isMain: Boolean = false,
)