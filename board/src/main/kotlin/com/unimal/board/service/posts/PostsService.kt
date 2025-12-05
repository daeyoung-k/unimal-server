package com.unimal.board.service.posts

import com.unimal.board.controller.request.PostsCreateRequest
import com.unimal.board.controller.request.PostsListRequest
import com.unimal.board.domain.board.BoardFile
import com.unimal.board.domain.board.like.BoardLike
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.posts.dto.BoardId
import com.unimal.board.service.posts.dto.LikeResponse
import com.unimal.board.service.posts.dto.PostInfo
import com.unimal.board.service.posts.manager.LikeManager
import com.unimal.board.service.posts.manager.PostsManager
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.AlreadyBeenProcessedException
import com.unimal.webcommon.exception.BoardNotFoundException
import com.unimal.webcommon.exception.ErrorCode
import com.unimal.webcommon.exception.UserNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class PostsService(
    private val filesManager: FilesManager,
    private val postsManager: PostsManager,
    private val likeManager: LikeManager,
    private val memberManager: MemberManager,

    @Value("\${etc.files.base-url}")
    private val fileBaseUrl: String,
    private val hashidsUtil: HashidsUtil,
) {

    val logger = KotlinLogging.logger {}

    @Transactional
    fun posting(
        userInfo: CommonUserInfo,
        postsCreateRequest: PostsCreateRequest,
        files: List<MultipartFile>?
    ): BoardId {

        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
//        val user = memberManager.findByEmail(userInfo.email) ?: BoardMember(email = userInfo.email, name = "Temp User")

        val location = postsManager.createLocationPointInfo(postsCreateRequest.longitude, postsCreateRequest.latitude)
        val board = postsManager.saveBoard(
            postsCreateRequest.toBoardCreateDto(user, location)
        )

        if (files?.isNotEmpty() == true) {
            files.forEachIndexed { index, file ->
                val main = index == 0
                val uploadFileInfo = filesManager.uploadFile(file)
                val fileUrl = fileBaseUrl + "/" + uploadFileInfo.key

                postsManager.saveBoardFile(
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
        val boardImageUrls = board.images.map { it.fileUrl ?: "" }

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
            likeCount = likeManager.getPostLike(board.id!!.toString()),
            replyCount = postsManager.getPostReply(board.id!!.toString()),
            reply = emptyList()
        )
    }

    fun getPostList(
        postsListRequest: PostsListRequest
    ) {
        postsManager.postList(postsListRequest)

    }

    @Transactional
    fun postLike(
        userInfo: CommonUserInfo,
        boardId: String
    ): LikeResponse {
        val id = hashidsUtil.decode(boardId)
        val board = postsManager.getReferenceBoard(id)

        try {
            // 좋아요가 있는지 확인
            val existingLike = likeManager.existingLike(board, userInfo.email)
            val isLiked = if (existingLike != null) {
                // 좋아요 삭제
                likeManager.deleteBoardLike(existingLike)
                false
            } else {
                // 좋아요 저장
                likeManager.saveBoardLike(
                    BoardLike(
                        board = board,
                        email = userInfo.email,
                    )
                )
                true
            }

            // 좋아요 캐시 업데이트
            val likeCount = likeManager.saveCachePostLikeGetCount(board = board)

            return LikeResponse(
                isLiked = isLiked,
                likeCount = likeCount
            )

        } catch (e: Exception) {
            e.printStackTrace()
            logger.error { "좋아요 오류: ${e.message}" }
            throw AlreadyBeenProcessedException()
        }
    }


}