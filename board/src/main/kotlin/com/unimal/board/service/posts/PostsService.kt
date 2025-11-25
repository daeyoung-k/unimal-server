package com.unimal.board.service.posts

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.posts.dto.BoardId
import com.unimal.board.service.posts.dto.PostInfo
import com.unimal.board.service.posts.manager.PostsManager
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.BoardNotFoundException
import com.unimal.webcommon.exception.ErrorCode
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
    ): BoardId {

        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
//        val user = memberManager.findByEmail(userInfo.email) ?: BoardMember(email = userInfo.email, name = "Temp User")

        val location = postsManager.createLocationPointInfo(postsCreateRequest.longitude, postsCreateRequest.latitude)
        val board = postsManager.savedBoard(
            postsCreateRequest.toBoardCreateDto(user, location)
        )

        if (files?.isNotEmpty() == true) {
            files.forEachIndexed { index, file ->
                val main = index == 0
                val uploadFileInfo = filesManager.uploadFile(file)
                val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key

                postsManager.savedBoardFile(
                    BoardFile(
                        board = board,
                        main = main,
                        fileName = uploadFileInfo.originalFilename,
                        fileKey = uploadFileInfo.key,
                        fileUrl = fileUrl
                    )
                )
            }
        }

        postsManager.createCachePostLikeAndReplyCount(board.id!!.toString())

        return BoardId(boardId = hashidsUtil.encode(board.id!!))
    }

    fun getPost(
        boardId: String
    ): PostInfo? {
        val id = hashidsUtil.decode(boardId)
        val board = postsManager.getBoard(id) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)

        val boardMember = board.email
        val boardImageUrls = postsManager.getBoardFilesUrls(board)

        return PostInfo(
            boardId = hashidsUtil.encode(board.id!!),
            email = boardMember.email,
            profileImage = boardMember.profileImage,
            nickname = boardMember.nickname ?: "",
            title = board.title ?: "",
            content = board.content,
            streetName = board.streetName!!,
            public = board.public,
            createdAt = board.createdAt,
            imageUrlList = boardImageUrls,
            likeCount = postsManager.getPostLike(boardId),
            replyCount = postsManager.getPostReply(boardId),
            reply = emptyList()
        )
    }


}