package com.unimal.board.service.post.enums

enum class UserCountCalculateType(
    private val description: String
) {
    INCREMENT("증가 타입"),
    DECREMENT("감소 타입")
}