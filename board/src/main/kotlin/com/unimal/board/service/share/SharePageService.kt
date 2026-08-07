package com.unimal.board.service.share

import com.unimal.board.domain.board.Board
import com.unimal.board.domain.board.BoardRepository
import com.unimal.board.enums.PostShow
import com.unimal.board.service.post.manager.LikeManager
import com.unimal.board.service.post.manager.ReplyManager
import com.unimal.board.service.share.dto.SharePage
import com.unimal.board.service.share.dto.ShareUnavailableReason
import com.unimal.board.utils.HashidsUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 공유 페이지 조립.
 *
 * 설계: `docs/specs/2026-08-07-게시글-공유.md`
 *
 * ## 공개 여부를 렌더 시점에 매번 다시 본다
 *
 * 앱에서 비공개 글의 공유 버튼을 숨기는 것만으로는 부족하다. **링크는 영원히 남지만
 * 공개 설정은 바뀐다** — 공개일 때 공유된 링크가 카톡 대화에 남아 있고, 그 뒤에
 * 작성자가 비공개로 돌릴 수 있다. 그래서 판정은 여기가 최종이다.
 *
 * ## 예외를 던지지 않는다
 *
 * [com.unimal.board.service.post.PostService] 는 글이 없으면 `BoardNotFoundException`
 * 을 던지고 전역 핸들러가 JSON 에러를 만든다. 공유 페이지는 **브라우저와 크롤러가
 * 여는 HTML 엔드포인트**라 JSON 을 돌려주면 안 된다.
 *
 * 게다가 없는 글·잘못된 hashid 요청은 정상적으로 발생한다 — 링크는 오래 살아남고
 * 크롤러는 아무 URL 이나 때린다. 그걸 예외로 처리하면 에러 로그가 노이즈로 찬다.
 * 그래서 [SharePageResult] 로 돌려주고 컨트롤러가 상태 코드를 붙인다.
 */
@Service
class SharePageService(
    private val boardRepository: BoardRepository,
    private val likeManager: LikeManager,
    private val replyManager: ReplyManager,
    private val hashidsUtil: HashidsUtil,
    private val shareUrlFactory: ShareUrlFactory,
) {

    private val logger = KotlinLogging.logger {}

    @Transactional(readOnly = true)
    fun load(encodedBoardId: String): SharePageResult {
        val board = findBoard(encodedBoardId)
            ?: return SharePageResult.Unavailable(ShareUnavailableReason.NOT_FOUND)

        unavailableReason(board)?.let { return SharePageResult.Unavailable(it) }

        return SharePageResult.Available(board.toSharePage())
    }

    /**
     * hashid 디코딩 실패를 "없는 글"과 같게 다룬다.
     *
     * [HashidsUtil.decode] 는 실패 시 `HashidsException` 을 던진다. 사람이 주소창에
     * 오타를 내거나 크롤러가 `/s/favicon.ico` 같은 걸 때리면 그게 500 이 된다.
     * 여기서 삼키고 404 로 보낸다 — **잘못된 ID 는 오류가 아니라 정상적인 입력**이다.
     */
    private fun findBoard(encodedBoardId: String): Board? {
        val id = runCatching { hashidsUtil.decode(encodedBoardId) }
            .getOrElse {
                logger.debug { "share page - invalid hashid: $encodedBoardId" }
                return null
            }
        return boardRepository.findBoardById(id)
    }

    private fun unavailableReason(board: Board): ShareUnavailableReason? = when {
        board.del -> ShareUnavailableReason.DELETED
        board.show == PostShow.BLOCKED -> ShareUnavailableReason.BLOCKED
        board.show != PostShow.PUBLIC -> ShareUnavailableReason.PRIVATE
        else -> null
    }

    /**
     * 엔티티 → 렌더 DTO.
     *
     * `boardId` 를 **경로로 받은 문자열이 아니라 여기서 다시 인코딩한다.** 페이지 안
     * JavaScript 에 그대로 들어가는 값이라, 입력을 그대로 흘리면 인젝션 경로가 된다
     * ([SharePageRenderer] KDoc 참고). 재인코딩하면 Hashids 알파벳만 남는다.
     */
    private fun Board.toSharePage(): SharePage {
        val mainImage = images.filterNotNull().firstOrNull()
        val encodedId = hashidsUtil.encode(id!!)
        return SharePage(
            boardId = encodedId,
            shareUrl = shareUrlFactory.of(encodedId),
            title = title,
            content = content,
            // 동 이름이 사람이 말하는 단위다. 없으면 도로명으로 폴백.
            place = dong?.takeIf { it.isNotBlank() } ?: streetName?.takeIf { it.isNotBlank() },
            nickname = email.nickname ?: "",
            profileImage = email.profileImage,
            // 지도 마커와 같은 규칙 — 400px 파생 우선, 백필 전 파일은 원본 폴백.
            imageUrl = mainImage?.thumbUrl ?: mainImage?.fileUrl,
            // og:image 는 우선순위가 반대다. 크롤러가 가져가는 값이라 커야 한다.
            ogImageUrl = mainImage?.fileUrl ?: mainImage?.thumbUrl,
            likeCount = likeManager.getCachePostLikeCount(id!!.toString()),
            replyCount = replyManager.getCachePostReplyCount(id!!.toString()),
            createdAt = createdAt,
        )
    }
}

sealed interface SharePageResult {
    data class Available(val page: SharePage) : SharePageResult
    data class Unavailable(val reason: ShareUnavailableReason) : SharePageResult
}
