package com.unimal.board.service.post

import com.unimal.board.controller.request.post.PostUpdateRequest
import com.unimal.board.controller.request.post.PostCreateRequest
import com.unimal.board.controller.request.post.PostFileDeleteRequest
import com.unimal.board.controller.request.post.PostListRequest
import com.unimal.board.controller.request.post.PostReplyRequest
import com.unimal.board.domain.board.like.BoardLike
import com.unimal.board.domain.board.reply.BoardReply
import com.unimal.board.domain.board.reply.toDto
import com.unimal.board.grpc.file.FileDeleteGrpcService
import com.unimal.board.service.files.FilesManager
import com.unimal.board.service.member.MemberManager
import com.unimal.board.service.post.dto.BoardFileInfo
import com.unimal.board.service.post.dto.BoardId
import com.unimal.board.service.post.dto.LikeResponse
import com.unimal.board.service.post.dto.PostInfo
import com.unimal.board.service.post.dto.Reply
import com.unimal.board.service.post.manager.LikeManager
import com.unimal.board.service.post.manager.PostManager
import com.unimal.board.service.post.manager.ReplyManager
import com.unimal.board.utils.HashidsUtil
import com.unimal.common.dto.CommonUserInfo
import com.unimal.webcommon.exception.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class PostService(
    private val filesManager: FilesManager,
    private val postManager: PostManager,
    private val likeManager: LikeManager,
    private val memberManager: MemberManager,
    private val replyManager: ReplyManager,

    private val hashidsUtil: HashidsUtil,
    private val fileDeleteGrpcService: FileDeleteGrpcService,

    ) {

    val logger = KotlinLogging.logger {}

    @Transactional
    fun posting(
        userInfo: CommonUserInfo,
        postCreateRequest: PostCreateRequest,
        files: List<MultipartFile>?
    ): BoardId {

        val user = memberManager.findByEmail(userInfo.email) ?: throw UserNotFoundException(ErrorCode.USER_NOT_FOUND.message)
        val location = postManager.createLocationPointInfo(postCreateRequest.longitude, postCreateRequest.latitude)
        val board = postManager.saveBoard(
            postCreateRequest.toBoardCreateDto(user, location)
        )

        // 파일 업로드 & 게시판 저장
        if (files?.isNotEmpty() == true) filesManager.uploadFile(board, files)

        postManager.createCachePostLikeAndReplyCount(board.id!!.toString())

        return BoardId(boardId = hashidsUtil.encode(board.id!!))
    }

    fun getPost(
        optionalUserInfo: CommonUserInfo?,
        encryptBoardId: String
    ): PostInfo? {
        val id = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(id) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)

        val boardMember = board.email
        val boardFileInfo = board.images.mapNotNull {
            if (it?.id == null) null else BoardFileInfo(fileId = hashidsUtil.encode(it.id!!), fileUrl = it.fileUrl!!)
        }

        val isOwner = if (optionalUserInfo != null) {
            boardMember.email == optionalUserInfo.email
        } else {
            false
        }

        return PostInfo(
            boardId = hashidsUtil.encode(board.id!!),
            email = boardMember.email,
            profileImage = boardMember.profileImage,
            nickname = boardMember.nickname ?: "",
            title = board.title ?: "",
            content = board.content,
            streetName = board.streetName!!,
            show = board.show,
            mapShow = board.mapShow,
            createdAt = board.createdAt,
            fileInfoList = boardFileInfo,
            likeCount = likeManager.getCachePostLikeCount(board.id!!.toString()),
            replyCount = replyManager.getCachePostReplyCount(board.id!!.toString()),
            reply = replyList(
                optionalUserInfo,
                encryptBoardId
            ),
            isOwner = isOwner
        )
    }

    fun getPostList(
        optionalUserInfo: CommonUserInfo?,
        postListRequest: PostListRequest
    ): List<PostInfo> {
        val boardList = postManager.getBoardConditionList(postListRequest)
        if (boardList.isEmpty()) return emptyList()

        // N+1 방지
        val idList = boardList.map { it.id!! }
        val boardFiles = postManager.getBoardFileInBoardIdList(idList)
        val ownerEmail = optionalUserInfo?.email ?: ""

        return boardList.map { board ->
            val boardMember = board.email
            val fileInfoList = boardFiles.mapNotNull {
                if (it.board == board) {
                    BoardFileInfo(fileId = hashidsUtil.encode(it.id!!), fileUrl = it.fileUrl!!)
                } else null
            }
            val isOwner = boardMember.email == ownerEmail
            val encryptBoardId = hashidsUtil.encode(board.id!!)
            PostInfo(
                boardId = encryptBoardId,
                email = boardMember.email,
                profileImage = boardMember.profileImage,
                nickname = boardMember.nickname ?: "",
                title = board.title ?: "",
                content = board.content,
                streetName = board.streetName!!,
                show = board.show,
                mapShow = board.mapShow,
                createdAt = board.createdAt,
                fileInfoList = fileInfoList,
                likeCount = likeManager.getCachePostLikeCount(board.id!!.toString()),
                replyCount = replyManager.getCachePostReplyCount(board.id!!.toString()),
                reply = replyList(
                    optionalUserInfo,
                    encryptBoardId
                ),
                isOwner = isOwner
            )
        }

    }

    @Transactional
    fun postLike(
        userInfo: CommonUserInfo,
        boardId: String
    ): LikeResponse {
        val id = hashidsUtil.decode(boardId)
        val board = postManager.getReferenceBoard(id)

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

    @Transactional
    fun postUpdate(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        postUpdateRequest: PostUpdateRequest
    ) {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        if (!postManager.postOwnerCheck(userInfo.email, board.email.email)) throw BoardOwnerException(ErrorCode.BOARD_OWNER_NOT_MATCH.message)

        if (!postUpdateRequest.title.isNullOrBlank() && board.title?.equals(postUpdateRequest.title) == false) {
            board.title = postUpdateRequest.title
            board.updatedAt = LocalDateTime.now()
        }

        if (!postUpdateRequest.content.isNullOrBlank() && board.content != postUpdateRequest.content) {
            board.content = postUpdateRequest.content
            board.updatedAt = LocalDateTime.now()
        }

        if (postUpdateRequest.isShow != null && board.show != postUpdateRequest.isShow) {
            board.show = postUpdateRequest.isShow
            board.updatedAt = LocalDateTime.now()
        }

        if (postUpdateRequest.isMapShow != null && board.mapShow != postUpdateRequest.isMapShow) {
            board.mapShow = postUpdateRequest.isMapShow
            board.updatedAt = LocalDateTime.now()
        }
    }

    @Transactional
    fun postDelete(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
    ) {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoardByIdAndEmail(boardId, userInfo.email) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)

        board.del = true
        board.updatedAt = LocalDateTime.now()
    }

    @Transactional
    fun postFileUpload(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        files: List<MultipartFile>
    ): List<BoardFileInfo> {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        if (!postManager.postOwnerCheck(userInfo.email, board.email.email)) throw BoardOwnerException(ErrorCode.BOARD_OWNER_NOT_MATCH.message)

        // main 파일이 있으면 true
        val mainCheck = board.images.any { it?.main == true }
        filesManager.uploadFile(board, files, mainCheck)

        val boardFiles = postManager.getBoardFileInBoardIdList(listOf(board.id!!))

        return boardFiles.map {
            BoardFileInfo(fileId = hashidsUtil.encode(it.id!!), fileUrl = it.fileUrl!!)
        }
    }

    @Transactional
    fun postFileDelete(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        postFileDeleteRequest: PostFileDeleteRequest
    ) {
        if (postFileDeleteRequest.fileIds.isEmpty()) return

        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        if (!postManager.postOwnerCheck(userInfo.email, board.email.email)) throw BoardOwnerException(ErrorCode.BOARD_OWNER_NOT_MATCH.message)

        val fileIdList = postFileDeleteRequest.fileIds.map { hashidsUtil.decode(it) }

        val boardFileList = postManager.getBoardFileInFileIdList(board, fileIdList)

        if (boardFileList.isNotEmpty()) {
            val fileKeys = boardFileList.mapNotNull { it.fileKey }
            fileDeleteGrpcService.deleteFile(fileKeys)

            postManager.deleteAllBoardFiles(boardFileList)
        }
    }

    @Transactional
    fun replyCreate(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        postReplyRequest: PostReplyRequest,
    ): Reply {

        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)

        val replyId = if (!postReplyRequest.replyId.isNullOrBlank()) {
            hashidsUtil.decode(postReplyRequest.replyId)
        } else {
            null
        }

        val reply = replyManager.saveReply(
            BoardReply(
                board = board,
                replyId = replyId,
                email = userInfo.email,
                comment = postReplyRequest.comment,
            )
        )

        val newReplyId = if (reply.replyId == null) null else hashidsUtil.encode(reply.replyId)

        // 댓글수 캐시 업데이트
        replyManager.saveCachePostReplyCount(board)

        return Reply(
            id = hashidsUtil.encode(reply.id!!),
            boardId = hashidsUtil.encode(board.id!!),
            replyId = newReplyId,
            reReplyYn = newReplyId != null,
            email = userInfo.email,
            nickname = userInfo.nickname,
            comment = reply.comment,
            createdAt = reply.createdAt.toString(),
            isOwner = true,
            isDel = reply.del ?: false
        )
    }

    fun replyList(
        optionalUserInfo: CommonUserInfo?,
        encryptBoardId: String,
    ): List<Reply> {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        val boardReplyList = replyManager.getBoardReplyList(board.id!!)
        return boardReplyList.map {
            val isOwner = optionalUserInfo?.email == it.email
            it.toDto(
                id = hashidsUtil.encode(it.id),
                boardId = hashidsUtil.encode(it.boardId),
                replyId = if (it.replyId != null) hashidsUtil.encode(it.replyId!!) else null,
                isOwner = isOwner,
            )
        }
    }

    @Transactional
    fun replyUpdate(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        encryptReplyId: String,
        postReplyRequest: PostReplyRequest
    ): Reply {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        val replyId = hashidsUtil.decode(encryptReplyId)

        val boardReply = replyManager.getBoardReplyIdAndBoardAndEmail(replyId, board, userInfo.email)
            ?: throw ReplyNotFoundException(ErrorCode.REPLY_NOT_FOUND.message)

        boardReply.comment = postReplyRequest.comment
        boardReply.updatedAt = LocalDateTime.now()

        val newReplyId = if (boardReply.replyId == null) null else hashidsUtil.encode(boardReply.replyId)
        return Reply(
            id = hashidsUtil.encode(boardReply.id!!),
            boardId = hashidsUtil.encode(board.id!!),
            replyId = newReplyId,
            reReplyYn = newReplyId != null,
            email = boardReply.email,
            nickname = userInfo.nickname,
            comment = boardReply.comment,
            createdAt = boardReply.createdAt.toString(),
            isOwner = true,
            isDel = boardReply.del ?: false
        )
    }

    @Transactional
    fun replyDelete(
        userInfo: CommonUserInfo,
        encryptBoardId: String,
        encryptReplyId: String,
    ) {
        val boardId = hashidsUtil.decode(encryptBoardId)
        val board = postManager.getBoard(boardId) ?: throw BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)
        val replyId = hashidsUtil.decode(encryptReplyId)

        val boardReply = replyManager.getBoardReplyIdAndBoardAndEmail(replyId, board, userInfo.email)
            ?: throw ReplyNotFoundException(ErrorCode.REPLY_NOT_FOUND.message)

        boardReply.del = true
        boardReply.updatedAt = LocalDateTime.now()

        // 댓글수 캐시 업데이트
        replyManager.saveCachePostReplyCount(board)
    }

}