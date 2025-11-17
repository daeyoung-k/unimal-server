package com.unimal.board.service.posts

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.posts.dto.PostsInfo
import com.unimal.board.service.posts.manager.PostsManager
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.UserNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class PostsService(
    private val filesManager: FilesManager,
    private val postsManager: PostsManager,
    private val memberManager: MemberManager,

    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,
    private val hashidsUtil: HashidsUtil,
) {

    @Transactional
    fun posting(
        userInfo: CommonUserInfo,
        postsCreateRequest: PostsCreateRequest,
        files: List<MultipartFile>?
    ): PostsInfo {

        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException("User not found")
//        val user = memberManager.findByEmail(userInfo.email) ?: BoardMember(email = userInfo.email, name = "Temp User")

        val location = postsManager.createLocationPointInfo(postsCreateRequest.longitude, postsCreateRequest.latitude)
        val board = postsManager.savedBoard(
            postsCreateRequest.toBoardCreateDto(user, location)
        )

        val imageUrlList = mutableListOf<String>()

        if (files?.isNotEmpty() == true) {
            files.forEachIndexed { index, file ->
                val main = index == 0
                val uploadFileInfo = filesManager.uploadFile(file)
                val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key

                val boardFile = postsManager.savedBoardFile(
                    BoardFile(
                        board = board,
                        main = main,
                        fileName = uploadFileInfo.originalFilename,
                        fileKey = uploadFileInfo.key,
                        fileUrl = fileUrl
                    )
                )
                imageUrlList.add(boardFile.fileUrl!!)
            }
        }

        return PostsInfo(
            boardId = hashidsUtil.encode(board.id!!),
            email = user.email,
            profileImage = user.profileImage,
            nickname = user.nickname ?: "",
            title = board.title ?: "",
            content = board.content,
            streetName = board.streetName!!,
            public = board.public,
            createdAt = board.createdAt,
            imageUrlList = imageUrlList
        )

    }

    fun getPost(
        boardId: String
    ) {
        val id = hashidsUtil.decode(boardId)
        println(id)
    }


}