package com.unimal.board.service.post.enums

enum class ZoomLevel(
    val level: Int,
    val radiusMeters: Double,
    val postLimit: Int,
) {
    // 가독성을 위한 _ 리터럴 값 적용
    ZOOM_10(10, 50_000.0, 30),
    ZOOM_11(11, 30_000.0, 30),
    ZOOM_12(12, 20_000.0, 30),
    ZOOM_13(13, 10_000.0, 40),
    ZOOM_14(14,  5_000.0, 40),
    ZOOM_15(15,  3_000.0, 50),
    ZOOM_16(16,  2_000.0, 50),
    ZOOM_17(17,  1_000.0, 50),
    ZOOM_18(18,    500.0, 100),
    ZOOM_19(19,    300.0, 100),
    ZOOM_20(20,    100.0, 100);

    companion object {
        // 기본 줌 값 리턴
        fun from(zoom: Int): ZoomLevel = entries.find { it.level == zoom } ?: ZOOM_14
    }
}