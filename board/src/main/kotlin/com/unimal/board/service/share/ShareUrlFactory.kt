package com.unimal.board.service.share

import com.unimal.board.enums.PostShow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * 공유 URL 생성.
 *
 * 설계: `docs/specs/2026-08-07-게시글-공유.md`
 *
 * ## 앱이 URL 을 조립하지 않는다
 *
 * 앱에서 `"https://..." + boardId` 로 만들면 안 된다. **앱은 배포하면 못 고친다.**
 * 도메인이나 경로를 바꿀 일이 생겼을 때 하드코딩된 클라이언트는 스토어 심사를 다시
 * 받아야 하고, 그 사이 구버전 사용자들은 죽은 링크를 계속 퍼뜨린다.
 * 서버가 내려주면 서버 배포 한 번으로 끝난다.
 *
 * ## null 이 곧 "공유 불가"다
 *
 * 공유할 수 없는 글은 URL 을 아예 만들지 않는다. 앱은 `shareUrl == null` 이면 공유
 * 버튼을 숨기면 되고, **"어떤 조건에서 공유 가능한가"를 앱이 알 필요가 없다.**
 * 나중에 친구 공개 정책이 바뀌어도 앱은 그대로다.
 *
 * 물론 이건 UI 용 판단일 뿐이고, 실제 차단은 [SharePageService] 가 렌더 시점에 다시 한다.
 * 링크는 영원히 남지만 공개 설정은 바뀌기 때문이다.
 */
@Component
class ShareUrlFactory(
    @Value("\${custom.share.base-url}")
    private val baseUrl: String,
) {

    /** 공유 가능 여부를 이미 아는 곳(공유 페이지 자신 등)에서 쓴다. */
    fun of(encodedBoardId: String): String =
        "${baseUrl.trimEnd('/')}$SHARE_PATH$encodedBoardId"

    /** API 응답용. 공유할 수 없는 글이면 null — 앱은 이걸로 버튼을 숨긴다. */
    fun ofShareable(encodedBoardId: String, show: PostShow, deleted: Boolean): String? {
        if (deleted || show != PostShow.PUBLIC) return null
        return of(encodedBoardId)
    }

    companion object {
        /** 공유 URL 경로. 이 값의 유일한 출처다 — 게이트웨이 `ShareRouteConfig` 와 맞춰야 한다. */
        private const val SHARE_PATH = "/s/"
    }
}
