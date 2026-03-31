package com.unimal.board.controller.notice.dto

import com.unimal.board.domain.notice.Notice
import com.unimal.board.enums.NoticeType
import jakarta.validation.constraints.NotBlank

data class NoticePostRequest(
    @field:NotBlank
    val type: NoticeType,
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val content: String
) {
    fun toNotice() = Notice(type = type, title = title, content = content)
}
