package com.unimal.board.service.share

import com.unimal.board.service.share.dto.SharePage
import com.unimal.board.service.share.dto.ShareUnavailableReason
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 공유 페이지 HTML 렌더러.
 *
 * 설계: `docs/specs/2026-08-07-게시글-공유.md`
 *
 * ## 왜 템플릿 엔진을 안 쓰나
 *
 * board 모듈에 Thymeleaf 가 없다. 페이지가 **하나**뿐인데 뷰 리졸버·템플릿 경로·
 * 캐시 설정을 새로 들이는 건 과하다. 대신 이스케이프를 직접 책임진다.
 *
 * ## 이스케이프 규칙 — 이 파일에서 가장 중요한 부분
 *
 * 제목·본문·닉네임은 **사용자 입력이 그대로 HTML 에 박히는 값**이다. 제목에
 * `<script>` 를 넣은 글 하나로 페이지가 뚫린다.
 *
 * 그래서 값을 넣는 통로를 둘로만 뒀다.
 *
 * - [esc] : 텍스트·속성값 전부. `&<>"'` 를 엔티티로 바꾼다.
 * - [safeUrl] : `src`/`href`/`og:image` 처럼 URL 이 들어가는 자리. 이스케이프에 더해
 *   **스킴을 검사한다.** `javascript:` 는 이스케이프를 통과하지만 클릭하면 실행된다.
 *
 * **문자열 보간에 값을 그냥 넣지 말 것.** `${'$'}{page.title}` 이 아니라
 * `${'$'}{esc(page.title)}` 이다. 새 필드를 추가할 때 이 규칙을 어기면 그 자리가
 * 곧 취약점이다.
 *
 * ## 딥링크 ID 는 다시 인코딩한 값을 쓴다
 *
 * 페이지 안 JavaScript 에 게시글 ID 가 들어간다. 여기에 **URL 로 받은 문자열을 그대로
 * 넣으면 JS 컨텍스트 인젝션이 열린다** — HTML 이스케이프는 `<script>` 안에서 방어가
 * 되지 않는다.
 *
 * [SharePage.boardId] 는 경로 입력이 아니라 조회에 성공한 뒤 서버가 Long ID 를 다시
 * 인코딩한 값이다. Hashids 알파벳(영숫자)만 나오므로 구조적으로 안전하다.
 * 이 불변식이 깨지면(예: 입력값을 그대로 담기 시작하면) 여기가 뚫린다.
 */
@Component
class SharePageRenderer(
    @Value("\${custom.share.scheme}")
    private val appScheme: String,

    @Value("\${custom.share.store.android}")
    private val androidStoreUrl: String,

    @Value("\${custom.share.store.ios}")
    private val iosStoreUrl: String,

    /** 사진 없는 글의 `og:image` 폴백. 생략하면 카톡 카드가 텍스트만 남는다. */
    @Value("\${custom.share.default-image}")
    private val defaultImageUrl: String,
) {

    fun render(page: SharePage): String {
        val displayTitle = page.title?.takeIf { it.isNotBlank() } ?: page.content.ellipsis(OG_TITLE_LENGTH)
        val meta = listOfNotNull(page.place, relativeTime(page.createdAt, LocalDateTime.now()))
            .joinToString(" · ")
        val ogDescription = listOfNotNull(
            meta.takeIf { it.isNotBlank() },
            page.content.ellipsis(OG_DESCRIPTION_LENGTH).takeIf { it.isNotBlank() },
        ).joinToString(" · ")

        // 중첩 raw string 을 피하려고 미리 만든다. `${'$'}{ ... """...""" ... }` 형태는
        // 컴파일러가 바깥 문자열의 끝으로 오인할 수 있다.
        val titleBlock = page.title
            ?.takeIf { it.isNotBlank() }
            ?.let { "<h1 class=\"title\">${esc(it)}</h1>" }
            .orEmpty()

        return """
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>${esc(displayTitle)} · 스토맵</title>
<meta property="og:type" content="article">
<meta property="og:site_name" content="스토맵">
<meta property="og:title" content="${esc(displayTitle)}">
<meta property="og:description" content="${esc(ogDescription)}">
<meta property="og:image" content="${safeUrl(page.imageUrl) ?: esc(defaultImageUrl)}">
<meta property="og:url" content="${esc(page.shareUrl)}">
<meta name="twitter:card" content="summary_large_image">
<meta name="robots" content="index, follow">
${styleBlock()}
</head>
<body>
<main class="card">
  ${imageBlock(page)}
  <div class="body">
    <div class="author">
      ${profileBlock(page)}
      <span class="nickname">${esc(page.nickname)}</span>
    </div>
    $titleBlock
    <p class="content">${esc(page.content.ellipsis(BODY_LENGTH))}</p>
    <div class="meta">${esc(meta)}</div>
    <div class="counts">
      <span>♥ ${page.likeCount}</span>
      <span>💬 ${page.replyCount}</span>
    </div>
  </div>
  <button id="open-app" class="cta" type="button">앱에서 이어보기</button>
  <p class="hint">스토맵에서 이 장소의 다른 이야기도 볼 수 있어요</p>
</main>
${openAppScript(page.boardId)}
</body>
</html>
        """.trimIndent()
    }

    /**
     * 볼 수 없는 글의 페이지.
     *
     * **OG 태그에 글 내용을 넣지 않는다.** 비공개로 바뀐 글의 링크가 카톡에 남아 있을 때
     * 미리보기 카드에 내용이 뜨면 막은 의미가 없다. `noindex` 도 같이 건다.
     */
    fun renderUnavailable(reason: ShareUnavailableReason): String = """
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>스토맵</title>
<meta property="og:site_name" content="스토맵">
<meta property="og:title" content="스토맵">
<meta property="og:description" content="지도 위에 남기는 우리 동네 이야기">
<meta property="og:image" content="${esc(defaultImageUrl)}">
<meta name="robots" content="noindex, nofollow">
${styleBlock()}
</head>
<body>
<main class="card empty">
  <h1 class="title">${esc(reason.title)}</h1>
  <p class="content">${esc(reason.description)}</p>
  <a class="cta" href="${esc(androidStoreUrl)}">스토맵 둘러보기</a>
</main>
</body>
</html>
    """.trimIndent()

    private fun imageBlock(page: SharePage): String {
        val url = safeUrl(page.imageUrl) ?: return ""
        return """<img class="photo" src="$url" alt="">"""
    }

    private fun profileBlock(page: SharePage): String {
        val url = safeUrl(page.profileImage)
            ?: return """<span class="avatar placeholder"></span>"""
        return """<img class="avatar" src="$url" alt="">"""
    }

    /**
     * 앱 열기 → 실패하면 스토어.
     *
     * 커스텀 스킴은 **성공했는지 알 방법이 없다.** 앱이 열리면 브라우저가 백그라운드로
     * 가면서 타이머가 멈추거나 `visibilitychange` 가 발생하는데, 이걸 신호로 쓴다.
     * 완벽하지 않지만(기기·브라우저마다 다르다) 실패 쪽으로 기울어도 스토어로 가므로
     * 최악이 "앱 있는 사람이 스토어를 본다" 정도다.
     *
     * Universal Links / App Links 를 붙이면 이 스크립트 자체가 필요 없어진다
     * (링크를 OS 가 가로챈다). 그때 이 블록을 지우면 된다.
     */
    private fun openAppScript(boardId: String): String = """
<script>
(function () {
  var SCHEME_URL = "$appScheme://post?id=$boardId";
  var STORE_ANDROID = "${esc(androidStoreUrl)}";
  var STORE_IOS = "${esc(iosStoreUrl)}";
  var ua = navigator.userAgent || "";
  var store = /iPhone|iPad|iPod/i.test(ua) ? STORE_IOS : STORE_ANDROID;

  document.getElementById("open-app").addEventListener("click", function () {
    var moved = false;
    function onHide() { moved = true; }
    document.addEventListener("visibilitychange", onHide);
    window.addEventListener("pagehide", onHide);

    window.location.href = SCHEME_URL;

    setTimeout(function () {
      document.removeEventListener("visibilitychange", onHide);
      window.removeEventListener("pagehide", onHide);
      if (!moved && !document.hidden) window.location.href = store;
    }, 1200);
  });
})();
</script>
    """.trimIndent()

    /** 앱 디자인 시스템(Primary #7AB3FF, 카드 radius 24, 버튼 radius 14/height 54)을 따른다. */
    private fun styleBlock(): String = """
<style>
  :root { --primary: #3578E5; --primary-light: #7AB3FF; --bg: #E8F2FF; --text: #1B2733; --muted: #6B7A8C; }
  * { box-sizing: border-box; }
  body {
    margin: 0; padding: 24px 16px 40px;
    font-family: Pretendard, -apple-system, BlinkMacSystemFont, "Apple SD Gothic Neo", "Noto Sans KR", sans-serif;
    color: var(--text);
    background: linear-gradient(180deg, #3578E5 0%, #7AB3FF 45%, #A8CCFF 100%);
    min-height: 100vh;
    display: flex; justify-content: center; align-items: flex-start;
  }
  .card {
    width: 100%; max-width: 480px;
    background: rgba(255,255,255,0.96); border-radius: 24px; overflow: hidden;
    box-shadow: 0 12px 32px rgba(27,39,51,0.18);
  }
  .card.empty { padding: 40px 24px; text-align: center; }
  .photo { display: block; width: 100%; aspect-ratio: 4/3; object-fit: cover; background: var(--bg); }
  .body { padding: 20px; }
  .author { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
  .avatar { width: 32px; height: 32px; border-radius: 16px; object-fit: cover; }
  .avatar.placeholder { background: var(--bg); display: inline-block; }
  .nickname { font-size: 14px; font-weight: 600; }
  .title { font-size: 20px; font-weight: 700; margin: 0 0 8px; line-height: 1.4; }
  .content { font-size: 15px; line-height: 1.6; margin: 0 0 16px; white-space: pre-wrap; word-break: break-word; }
  .meta, .counts { font-size: 13px; color: var(--muted); }
  .counts { margin-top: 8px; display: flex; gap: 12px; }
  .cta {
    display: block; width: calc(100% - 40px); height: 54px; margin: 4px 20px 20px;
    border: 0; border-radius: 14px; cursor: pointer;
    background: linear-gradient(180deg, #3578E5 0%, #5B9FEF 100%);
    color: #fff; font-size: 16px; font-weight: 700; font-family: inherit;
    line-height: 54px; text-align: center; text-decoration: none;
  }
  .hint { margin: 0 20px 20px; font-size: 12px; color: var(--muted); text-align: center; }
</style>
    """.trimIndent()

    companion object {
        /** 카톡 미리보기 카드에서 잘리지 않는 대략적 길이. 넘으면 어차피 `…` 로 잘린다. */
        private const val OG_TITLE_LENGTH = 40
        private const val OG_DESCRIPTION_LENGTH = 80

        /**
         * 웹에 보여주는 본문 길이.
         *
         * 전문을 보여주면 앱을 깔 이유가 없어진다. 반대로 너무 짧으면 낚시로 읽힌다.
         * 300자는 "무슨 이야긴지는 알겠는데 끝은 궁금한" 정도다.
         */
        private const val BODY_LENGTH = 300

        /**
         * `src`/`href` 에 허용하는 스킴. `javascript:` 를 막는 게 목적이다.
         *
         * 이미지 URL 은 우리 CDN 에서 오지만, 지금 안전하다는 이유로 검사를 빼면
         * 나중에 외부 URL 을 담게 됐을 때 조용히 뚫린다.
         */
        private val ALLOWED_URL_PREFIXES = listOf("https://", "http://")

        private const val MINUTES_PER_DAY = 60L * 24
    }

    private fun esc(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        val sb = StringBuilder(value.length + 16)
        for (ch in value) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&#39;")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun safeUrl(value: String?): String? {
        val url = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (ALLOWED_URL_PREFIXES.none { url.startsWith(it, ignoreCase = true) }) return null
        return esc(url)
    }

    /**
     * 본문 자르기. 자를 때 공백 정리도 같이 한다 — 줄바꿈이 잔뜩 든 글이
     * `og:description` 에 들어가면 미리보기 카드가 이상해진다.
     */
    private fun String.ellipsis(limit: Int): String {
        val flat = trim().replace(WHITESPACE, " ")
        return if (flat.length <= limit) flat else flat.take(limit).trimEnd() + "…"
    }

    /**
     * 상대 시간 표기. 앱의 `relative_time.dart` 와 문구를 맞춘다.
     *
     * 절대 시각을 쓰지 않는 이유: 공유 링크는 몇 달 뒤에도 열린다. "2026.05.14" 보다
     * "3개월 전"이 글의 신선도를 바로 알려준다. 다만 1년이 넘어가면 상대 표기가
     * 오히려 감이 안 와서 날짜로 바꾼다.
     */
    private fun relativeTime(createdAt: LocalDateTime, now: LocalDateTime): String {
        val minutes = Duration.between(createdAt, now).toMinutes().coerceAtLeast(0)
        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            minutes < MINUTES_PER_DAY -> "${minutes / 60}시간 전"
            // 30일 미만까지 "일"로 센다. 여기서 7일로 끊고 "개월"로 넘어가면
            // 10일 된 글이 "0개월 전"이 된다.
            minutes < MINUTES_PER_DAY * 30 -> "${minutes / MINUTES_PER_DAY}일 전"
            minutes < MINUTES_PER_DAY * 365 -> "${minutes / (MINUTES_PER_DAY * 30)}개월 전"
            else -> createdAt.format(DATE_FORMAT)
        }
    }
}

private val WHITESPACE = Regex("\\s+")
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
