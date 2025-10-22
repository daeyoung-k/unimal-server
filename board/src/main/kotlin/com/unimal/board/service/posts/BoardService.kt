package com.unimal.board.service.posts

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.member.BoardMember
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.posts.dto.BoardPostingResponse
import com.unimal.board.service.posts.manager.BoardManager
import com.unimal.common.dto.CommonUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class BoardService(
    private val filesManager: FilesManager,
    private val boardManager: BoardManager,
    private val memberManager: MemberManager,

    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,
) {

    @Transactional
    fun posting(
        userInfo: CommonUserInfo,
        postsCreateRequest: PostsCreateRequest,
        files: List<MultipartFile>?
    ): BoardPostingResponse {

//        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException("User not found")
        val user = memberManager.findByEmail(userInfo.email)
            ?: memberManager.saveMember(BoardMember(email = userInfo.email, name = "Temp User"))

        val board = boardManager.saveBoard(
            postsCreateRequest.toBoardCreateDto(user)
        )

        if (files?.isNotEmpty() == true) {

            files.forEachIndexed { index, file ->
                val main = index == 0

                val uploadFileInfo = filesManager.uploadFile(file)

                boardManager.saveBoardFile(
                    BoardFile(
                        board = board,
                        main = main,
                        fileName = uploadFileInfo.originalFilename,
                        fileKey = uploadFileInfo.key,
                        fileUrl = fileBaseUrl + "/" + uploadFileInfo.key
                    )
                )
            }
        }

        return BoardPostingResponse(
            boardId = board.id.toString(),
            title = board.title ?: "",
            content = board.content,
            public = board.public
        )

    }


}