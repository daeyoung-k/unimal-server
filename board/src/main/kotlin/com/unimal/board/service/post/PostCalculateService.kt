package com.unimal.board.service.post

import com.unimal.board.domain.board.BoardRepository
import com.unimal.board.domain.board.like.BoardLikeRepository
import com.unimal.board.kafka.topics.dto.UserCountIssue
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
        userCountIssue: UserCountIssue
    ) {
        val totalLikeCount = getLikeTotalCount(userCountIssue.email)
        when (userCountIssue.type) {
            UserCountCalculateType.INCREMENT -> likeManager.saveUserTotalLikeCount(userCountIssue.email, totalLikeCount + 1)
            UserCountCalculateType.DECREMENT -> {
                if (totalLikeCount > 0) {
                    likeManager.saveUserTotalLikeCount(userCountIssue.email, totalLikeCount - 1)
                } else {
                    likeManager.saveUserTotalLikeCount(userCountIssue.email, 0)
                }
            }
        }
    }

    fun postCountCalculate(
        userCountIssue: UserCountIssue
    ) {
        val totalPostCount = getPostTotalCount(userCountIssue.email)
        when (userCountIssue.type) {
            UserCountCalculateType.INCREMENT -> postManager.saveUserTotalPostCount(userCountIssue.email, totalPostCount + 1)
            UserCountCalculateType.DECREMENT -> {
                if (totalPostCount > 0) {
                    postManager.saveUserTotalPostCount(userCountIssue.email, totalPostCount - 1)
                } else {
                    postManager.saveUserTotalPostCount(userCountIssue.email, 0)
                }
            }
        }
    }

    // 캐시 드리프트 방지: Redis 캐시(TTL 없음, 카프카 이벤트로만 증감)는 이벤트
    // 유실 시 실제값과 영구히 어긋난다. 받은 좋아요 수는 조회 빈도가 낮아 매번
    // DB 집계해도 비용 부담이 없으므로 캐시를 거치지 않고 DB에서 직접 센다.
    fun getLikeTotalCount(
        email: String
    ): Long {
        return boardLikeRepository.getUserTotalLikeCount(email)
    }

    // 캐시 드리프트 방지: Redis 캐시 대신 DB에서 직접 집계한다(del=false 전체 글).
    // 게시글 수 count는 인덱스로 가벼우며 마이페이지 진입 시에만 호출된다.
    fun getPostTotalCount(
        email: String
    ): Long {
        return boardRepository.getUserTotalPostCount(email)
    }

    fun getLikedStoriesCount(
        email: String
    ): Long {
        return boardLikeRepository.getLikedStoriesTotalCount(email)
    }
}