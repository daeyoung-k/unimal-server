package com.unimal.board.controller.share

import com.unimal.board.service.share.SharePageRenderer
import com.unimal.board.service.share.SharePageResult
import com.unimal.board.service.share.SharePageService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 게시글 공유 페이지. **이 저장소에서 유일하게 HTML 을 내려주는 API 엔드포인트다.**
 *
 * 설계: `docs/specs/2026-08-07-게시글-공유.md`
 *
 * ## 경로
 *
 * 이 모듈의 `context-path` 가 `/board` 라서 실제 경로는 `/board/s/{boardId}` 지만,
 * **외부에 노출되는 URL 은 `/s/{boardId}` 다.** api-gateway 가 RewritePath 로 바꿔준다
 * (`ShareRouteConfig` 참고). 공유 링크는 카톡·문자에 그대로 보이므로 짧아야 한다.
 *
 * 이 저장소에서 게이트웨이 경로와 서비스 경로가 다른 유일한 케이스다.
 *
 * ## 인증이 없다
 *
 * 게이트웨이에서 필터를 붙이지 않는다. 공유 페이지는 **비로그인 + 앱 미설치 상태에서
 * 열리는 것이 정상**이다. 카카오·슬랙 크롤러도 당연히 비로그인이다.
 *
 * 그래서 여기 나가는 값은 전부 "아무나 봐도 되는" 것이어야 한다. 작성자 이메일이
 * 새지 않도록 [com.unimal.board.service.share.dto.SharePage] 로 한 번 거른다.
 *
 * ## 왜 board 에 있나 — 그리고 언제 나가나
 *
 * 도메인 모듈에 HTML 이 있는 건 확실히 어색하다. 그럼에도 여기 있는 이유는 대안이
 * 전부 더 나쁘기 때문이다.
 *
 * - **api-gateway**: gRPC·proto 의존성이 아예 없다. 붙이면 인증·라우팅만 하던
 *   게이트웨이가 board 도메인을 알게 된다.
 * - **admin**: `anyRequest().authenticated()` 가 기본인 모듈이다. 여기에 공개
 *   엔드포인트를 뚫으면 **나중에 admin 보안을 조일 때 공유 페이지가 같이 막힌다.**
 *   게다가 admin 은 `unimal_admin` 스키마만 봐서 게시글을 읽지도 못한다.
 * - **새 `web` 모듈**: 장기적으로 옳지만, 페이지 하나 때문에 Dockerfile·compose
 *   서비스·CI·모니터링 대상이 늘어난다. 1인 운영에서 프로세스가 하나 느는 게
 *   가장 비싸다.
 *
 * **분리 기준(스펙 §4):** 공개 웹 페이지가 3개 이상이 되고, 그중 하나라도 board 밖
 * 데이터가 필요해질 때 `web` 모듈로 뺀다. 그때까지 `service/share/` 안에 격리해 두면
 * 옮기는 비용이 작게 유지된다 — **공유 관련 코드를 board 의 다른 곳에 흩지 말 것.**
 */
@RestController
@RequestMapping("/s")
class ShareController(
    private val sharePageService: SharePageService,
    private val sharePageRenderer: SharePageRenderer,
) {

    /**
     * 볼 수 없는 글도 **200 이 아니라 404** 로 내려간다.
     *
     * 200 으로 "차단된 글입니다"를 주면 어떤 글이 차단됐는지 외부에서 열거할 수 있고,
     * 검색엔진이 빈 페이지를 색인한다. 본문(HTML)은 그대로 주되 상태 코드로 구분한다 —
     * 브라우저는 404 여도 body 를 렌더하므로 사용자에게는 안내가 보인다.
     */
    @GetMapping("/{boardId}", produces = [MediaType.TEXT_HTML_VALUE])
    fun sharePage(@PathVariable boardId: String): ResponseEntity<String> =
        when (val result = sharePageService.load(boardId)) {
            is SharePageResult.Available ->
                ResponseEntity.ok(sharePageRenderer.render(result.page))

            is SharePageResult.Unavailable ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(sharePageRenderer.renderUnavailable(result.reason))
        }
}
