package com.unimal.photo.service.s3.dto

import java.nio.file.Path

data class MultipleFiles(
    val originalFilename: String,
    val key: String,
    val path: Path
)
