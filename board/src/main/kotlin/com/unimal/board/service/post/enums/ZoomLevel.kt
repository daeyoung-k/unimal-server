package com.unimal.board.service.post.enums

enum class ZoomLevel(
    val level: Int,
    val radiusMeters: Double,
    val postLimit: Int,
) {
    // 가독성을 위한 _ 리터럴 값 적용
    // 6~9 추가 (2026-07-19): 앱 최대 줌아웃 10 → 6 확장.
    // 반경은 줌 한 단계당 2배 규칙 유지 — 조회 반경은 항상 뷰포트를 덮는다.
    // 6(800km)은 한반도 전체를 덮는다.
    ZOOM_6(6, 800_000.0, 30),
    ZOOM_7(7, 400_000.0, 30),
    ZOOM_8(8, 200_000.0, 30),
    ZOOM_9(9, 100_000.0, 30),
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

    // 100m → 300m (2026-07-15): 폰 세로 뷰포트는 정줌 20에서도 상하 ~200m+ 를
    // 보여줘, 100m 반경은 화면에 보이는 글을 조회에서 누락시킨다
    // (줌인 중 마커가 삭제되고 밀집 판정이 뒤집히는 버그의 원인).
    // 조회 반경은 항상 뷰포트를 덮어야 한다.
    ZOOM_20(20,    300.0, 100);

    companion object {
        // 테이블 범위(6~20) 밖 줌은 가까운 경계로 클램프 —
        // 기존 폴백(ZOOM_14, 5km)은 줌 21(네이버 최대)에서 반경이
        // 오히려 50배 커지는 역전이 있었다.
        fun from(zoom: Int): ZoomLevel {
            val clamped = zoom.coerceIn(ZOOM_6.level, ZOOM_20.level)
            return entries.find { it.level == clamped } ?: ZOOM_14
        }
    }
}