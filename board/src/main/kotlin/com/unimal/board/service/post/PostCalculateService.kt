package com.unimal.board.service.post

import com.unimal.board.domain.board.BoardRepository
import com.unimal.board.domain.board.like.BoardLikeRepository
import com.unimal.board.kafka.topics.dto.UserCountIssueType
import com.unimal.board.service.post.enums.UserCountCalculateType
import com.unimal.board.service.post.manager.LikeManager
import com.unimal.board.service.post.manager.PostManager
import org.springframework.stereotype.Service

@Service
class PostCalculateService(
    private val boardLikeRepository: BoardLikeRepository,
    private val boardRepository: BoardRepository,

    private val likeManager: LikeManager,
    private val postManager: PostManager,
) {

    fun likeCountCalculate(
        userCountIssueType: UserCountIssueType
    ) {
        val totalLikeCount = getLikeTotalCount(userCountIssueType.email)
        when (userCountIssueType.type) {
            UserCountCalculateType.INCREMENT -> likeManager.saveUserTotalLikeCount(userCountIssueType.email, totalLikeCount + 1)
            UserCountCalculateType.DECREMENT -> {
                if (totalLikeCount > 0) {
                    likeManager.saveUserTotalLikeCount(userCountIssueType.email, totalLikeCount - 1)
                } else {
                    likeManager.saveUserTotalLikeCount(userCountIssueType.email, 0)
                }
            }
        }
    }

    fun postCountCalculate(
        userCountIssueType: UserCountIssueType
    ) {
        val totalPostCount = getPostTotalCount(userCountIssueType.email)
        when (userCountIssueType.type) {
            UserCountCalculateType.INCREMENT -> postManager.saveUserTotalPostCount(userCountIssueType.email, totalPostCount + 1)
            UserCountCalculateType.DECREMENT -> {
                if (totalPostCount > 0) {
                    postManager.saveUserTotalPostCount(userCountIssueType.email, totalPostCount - 1)
                } else {
                    postManager.saveUserTotalPostCount(userCountIssueType.email, 0)
                }
            }
        }
    }

    fun getLikeTotalCount(
        email: String
    ): Long {
        val totalLikeCount = likeManager.getUserTotalLikeCount(email)
        return if (totalLikeCount == null) {
            val totalDbCount = boardLikeRepository.getUserTotalLikeCount(email)
            likeManager.saveUserTotalLikeCount(email, totalDbCount)
            totalDbCount
        } else {
            totalLikeCount
        }

    }

    fun getPostTotalCount(
        email: String
    ): Long {
        val totalPostCount = postManager.getUserTotalPostCount(email)
        return if (totalPostCount == null) {
            val totalDbCount = boardRepository.getUserTotalPostCount(email)
            postManager.saveUserTotalPostCount(email, totalDbCount)
            totalDbCount
        } else {
            totalPostCount
        }
    }
}