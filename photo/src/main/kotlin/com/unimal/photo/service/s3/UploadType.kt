package com.unimal.photo.service.s3

enum class UploadType(
    val description: String,
    val path: String,
) {
    IMAGE("이미지 파일", "images/"),
    VIDEO("비디오 파일", "videos/"),
    ETC("기타 파일", "etc/");

    companion object {
        fun from(value: String): UploadType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: ETC
        }
    }
}