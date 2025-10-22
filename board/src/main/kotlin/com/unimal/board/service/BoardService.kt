package com.unimal.board.service

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.member.BoardMember
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.posts.BoardManager
import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.UserNotFoundException
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
    ) {

//        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException("User not found")
        val user = memberManager.findByEmail(userInfo.email) ?: BoardMember(email = userInfo.email, name = "Temp User")

        val board = boardManager.savedBoard(
            postsCreateRequest.toBoardCreateDto(user)
        )

        if (files?.isNotEmpty() == true) {

            files.forEachIndexed { index, file ->
                val main = index == 0

                val uploadFileInfo = filesManager.uploadFile(file)

                boardManager.savedBoardFile(
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

    }


}