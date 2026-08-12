package com.unimal.admin.domain.board.actionlog

enum class BoardPostActionType(
    val description: String,
) {
    BOARD_BLOCK("게시글 블락"),
    BOARD_UNBLOCK("게시글 블락 해제"),
}
