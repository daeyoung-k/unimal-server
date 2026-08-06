package com.unimal.board.service.share

import com.unimal.board.service.share.dto.SharePage
import com.unimal.board.service.share.dto.ShareUnavailableReason
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 공유 페이지 렌더러 테스트.
 *
 * **이스케이프에 집중한다.** 이 페이지는 인증 없이 아무나 여는 HTML 이고, 제목·본문·
 * 닉네임이 사용자 입력 그대로 들어간다. 여기가 뚫리면 공유 링크를 받은 사람 전부가
 * 대상이 된다. 레이아웃이 어긋나는 건 눈으로 보면 알지만, 이스케이프 누락은
 * 눈에 안 보인다 — 그래서 테스트가 필요한 쪽은 이쪽이다.
 */
class SharePageRendererTest {

    private val renderer = SharePageRenderer(
        appScheme = "stomap",
        androidStoreUrl = "https://play.google.com/store/apps/details?id=com.unimal.android.stomap",
        iosStoreUrl = "https://apps.apple.com/kr/app/stomap/id1",
        defaultImageUrl = "https://cdn.unimal.co.kr/static/share-default.png",
    )

    private fun page(
        title: String? = "날씨가 너무 좋당",
        content: String = "오늘 산책하다 발견한 골목",
        nickname: String = "대영",
        imageUrl: String? = "https://cdn.unimal.co.kr/images/a.jpg",
        profileImage: String? = null,
    ) = SharePage(
        boardId = "aBc123",
        shareUrl = "https://stomap.unimal.co.kr/s/aBc123",
        title = title,
        content = content,
        place = "역삼동",
        nickname = nickname,
        profileImage = profileImage,
        imageUrl = imageUrl,
        likeCount = 3,
        replyCount = 1,
        createdAt = LocalDateTime.now().minusHours(2),
    )

    @Test
    fun `제목에 들어온 스크립트 태그는 이스케이프된다`() {
        val html = renderer.render(page(title = "<script>alert(1)</script>"))

        assertFalse(html.contains("<script>alert(1)</script>"), "스크립트가 원문 그대로 남았다")
        assertContains(html, "&lt;script&gt;alert(1)&lt;/script&gt;")
    }

    @Test
    fun `따옴표로 og 속성값을 탈출할 수 없다`() {
        // 속성값 안에서 따옴표를 닫고 새 속성을 여는 고전적인 수법.
        val html = renderer.render(page(title = """" onload="alert(1)"""))

        assertFalse(html.contains("""onload="alert(1)""""), "속성값을 탈출했다")
        assertContains(html, "&quot;")
    }

    @Test
    fun `닉네임과 본문도 이스케이프된다`() {
        val html = renderer.render(
            page(nickname = "<b>굵게</b>", content = "<img src=x onerror=alert(1)>")
        )

        assertFalse(html.contains("<b>굵게</b>"))
        assertFalse(html.contains("<img src=x onerror=alert(1)>"))
    }

    @Test
    fun `javascript 스킴 이미지 URL 은 통째로 버린다`() {
        // 이스케이프만으로는 못 막는다. 이 문자열에는 이스케이프 대상 문자가 없어서
        // esc() 를 그냥 통과하고, src 에 들어가면 실행된다.
        val html = renderer.render(page(imageUrl = "javascript:alert(1)"))

        assertFalse(html.contains("javascript:alert(1)"))
        // 사진이 없는 것으로 처리되므로 og:image 는 기본 이미지로 폴백한다.
        assertContains(html, "share-default.png")
    }

    @Test
    fun `사진이 없으면 og image 는 기본 이미지로 폴백한다`() {
        val html = renderer.render(page(imageUrl = null))

        assertContains(html, """<meta property="og:image" content="https://cdn.unimal.co.kr/static/share-default.png">""")
        assertFalse(html.contains("""class="photo""""), "사진이 없는데 img 태그가 있다")
    }

    @Test
    fun `제목이 없으면 본문 앞부분이 og title 이 된다`() {
        // 비워두면 카톡 카드에 URL 이 그대로 노출된다.
        val html = renderer.render(page(title = null, content = "제목 없이 쓴 글"))

        assertContains(html, """<meta property="og:title" content="제목 없이 쓴 글">""")
        assertFalse(html.contains("<h1"), "제목이 없는데 h1 이 렌더됐다")
    }

    @Test
    fun `딥링크 스킴과 게시글 ID 가 스크립트에 들어간다`() {
        val html = renderer.render(page())

        assertContains(html, "stomap://post?id=aBc123")
    }

    @Test
    fun `볼 수 없는 글 페이지는 색인을 막고 글 내용을 담지 않는다`() {
        val html = renderer.renderUnavailable(ShareUnavailableReason.PRIVATE)

        assertContains(html, """<meta name="robots" content="noindex, nofollow">""")
        assertContains(html, "비공개 이야기예요")
        // 비공개로 바뀐 글의 링크가 카톡에 남아 있을 때 미리보기로 내용이 새면 안 된다.
        assertFalse(html.contains("og:description\" content=\"오늘"))
    }

    @Test
    fun `상대 시간은 일에서 개월로 넘어갈 때 0개월이 되지 않는다`() {
        // 7일 컷으로 나누면 10일 된 글이 "0개월 전"이 된다. 실제로 한 번 겪은 실수라
        // 경계값을 박아둔다.
        val html = renderer.render(
            page().copy(createdAt = LocalDateTime.now().minusDays(10))
        )

        assertContains(html, "10일 전")
        assertFalse(html.contains("0개월 전"))
    }

    @Test
    fun `본문이 길면 잘라서 보여준다`() {
        // 전문을 웹에 다 보여주면 앱을 깔 이유가 없어진다.
        val long = "가".repeat(500)
        val html = renderer.render(page(content = long))

        assertFalse(html.contains("가".repeat(400)), "본문이 잘리지 않았다")
        assertContains(html, "…")
    }

    @Test
    fun `줄바꿈이 많은 본문도 og description 을 망가뜨리지 않는다`() {
        val html = renderer.render(page(title = "제목", content = "첫 줄\n\n\n둘째 줄"))

        val ogLine = html.lines().first { it.startsWith("""<meta property="og:description"""") }
        assertEquals(1, ogLine.lines().size)
        assertTrue(ogLine.contains("첫 줄 둘째 줄"), "연속 공백이 정리되지 않았다: $ogLine")
    }
}
