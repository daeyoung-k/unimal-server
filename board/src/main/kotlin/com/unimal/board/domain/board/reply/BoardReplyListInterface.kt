package com.unimal.board.domain.board.reply


import com.unimal.board.service.post.dto.Reply

interface BoardReplyListInterface {
    val id: Long
    val boardId: Long
    val replyId: Long?
    val email: String
    val nickname: String?
    val comment: String
    val createdAt: String
    val del: Boolean?
}

fun BoardReplyListInterface.toDto(
    id: String,
    boardId: String,
    replyId: String?,
    isOwner: Boolean = false,
): Reply {
    return Reply(
        id = id,
        boardId = boardId,
        replyId = replyId,
        reReplyYn = replyId != null,
        email = email,
        nickname = nickname ?: "",
        comment = comment,
        createdAt = createdAt,
        isOwner = isOwner,
        isDel = del ?: false
    )
}