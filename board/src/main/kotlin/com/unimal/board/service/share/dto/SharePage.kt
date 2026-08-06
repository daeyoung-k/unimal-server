package com.unimal.board.service.share.dto

import java.time.LocalDateTime

/**
 * 공유 페이지 렌더에 필요한 값만 담는다.
 *
 * [com.unimal.board.domain.board.Board] 엔티티를 렌더러에 그대로 넘기지 않는다.
 * 엔티티에는 작성자 이메일처럼 **웹에 절대 나가면 안 되는 값**이 붙어 있고, 렌더러가
 * 엔티티를 알면 나중에 필드를 하나 추가할 때 실수로 노출하기 쉽다. 여기서 한 번
 * 걸러 담는 것이 유일한 통로다.
 */
data class SharePage(
    /**
     * Hashids 인코딩 값.
     *
     * **경로로 받은 문자열이 아니라, 조회 성공 후 서버가 Long ID 를 다시 인코딩한 값이다.**
     * 이 값은 페이지 안 JavaScript 에 들어가므로 입력을 그대로 흘리면 인젝션이 열린다.
     * 자세한 내용은 `SharePageRenderer` KDoc 참고.
     */
    val boardId: String,
    /** `og:url` 에 넣을 정규 URL. `ShareUrlFactory` 가 유일한 출처다. */
    val shareUrl: String,
    val title: String?,
    val content: String,
    /** 동 이름. 없으면 도로명, 그것도 없으면 null. */
    val place: String?,
    val nickname: String,
    val profileImage: String?,
    /** 대표 이미지. 400px 썸네일 우선, 없으면 원본, 사진 없는 글이면 null. */
    val imageUrl: String?,
    val likeCount: Long,
    val replyCount: Long,
    val createdAt: LocalDateTime,
)

/**
 * 공유 링크를 열었는데 글을 보여줄 수 없는 경우.
 *
 * **전부 404 로 내려간다.** 상태를 구분해서 200 으로 알려주면 어떤 글이 차단됐는지
 * 외부에서 열거할 수 있다. 문구만 다르게 해서 사용자가 상황은 알 수 있게 한다.
 */
enum class ShareUnavailableReason(
    val title: String,
    val description: String,
) {
    NOT_FOUND("찾을 수 없는 이야기예요", "주소가 잘못되었거나 사라진 이야기입니다."),
    DELETED("삭제된 이야기예요", "작성자가 이 이야기를 지웠습니다."),
    PRIVATE("비공개 이야기예요", "작성자만 볼 수 있도록 설정된 이야기입니다."),
    BLOCKED("볼 수 없는 이야기예요", "운영 정책에 따라 숨겨진 이야기입니다."),
}
