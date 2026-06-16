# 게시글/댓글/유저 신고 기능 설계 (board 모듈)

> 범위: **신고 접수 API만** (앱 → 서버). 어드민 처리 API/화면은 다음 작업으로 분리.
> 처리 정책: **전부 수동 검토** (자동 블라인드 없음 → `Board`에 blind 컬럼 추가하지 않음).

## 1. 현재 상태 (있는 것 / 빈 구멍)

이미 있는 뼈대:
- `Report` 엔티티 / `report` 테이블 (reporterEmail, targetType, targetId, reason, description, status, adminMemo, createdAt, reviewedAt)
- `ReportController` — `POST /report`
- `ReportService.saveReport()` — **받아서 그냥 저장만 함**
- `ReportCreateRequest` DTO
- enum: `ReportTargetType(POST/REPLY/USER)`, `ReportStatus(PENDING/REVIEWED/DISMISSED/SUCCESSED)`

빈 구멍 (이번에 채울 것):
1. 신고 사유가 자유 String → **enum 고정** 필요 (통계/악용 방지)
2. `targetId`가 Long → 앱이 가진 식별자(hashids String / email)와 불일치
3. 중복 신고 방지 없음
4. 자기 글/댓글/본인 신고 방지 없음
5. 신고 대상(게시글/댓글/유저) 존재·삭제 여부 검증 없음
6. `ReportRepository`에 조회 메서드 0개

## 2. 데이터 모델

### 2-1. ReportReason enum (신규)
`common/src/main/kotlin/com/unimal/common/enums/report/ReportReason.kt`

```kotlin
package com.unimal.common.enums.report

enum class ReportReason(val description: String) {
    SPAM("스팸/광고"),
    ABUSE("욕설/비방/혐오"),
    SEXUAL("음란물/선정성"),
    FALSE_INFO("허위정보"),
    ILLEGAL("불법정보"),
    PRIVACY("개인정보 노출"),
    ETC("기타"),
}
```

- `ETC`일 때만 `description`(자유 텍스트) **필수**. 나머지는 description 선택.

### 2-2. ReportStatus enum 정비
`common/.../enums/report/ReportStatus.kt` — 기존 네이밍 정리.

기존 (`REVIEWED="처리중"` 의미 충돌, `SUCCESSED` 오타):
```kotlin
PENDING("대기중"), REVIEWED("처리중"), DISMISSED("반려"), SUCCESSED("성공")
```

변경:
```kotlin
enum class ReportStatus(val description: String) {
    PENDING("접수"),      // 신고 접수, 미처리 (기본값)
    RESOLVED("처리완료"), // 신고 인정 → 대상 제재
    REJECTED("반려"),     // 신고 기각 → 문제 없음
}
```
- 상태 흐름: `PENDING → RESOLVED | REJECTED` (1인 운영이라 "검토중" 중간 상태 생략).
- **"처리 여부"는 별도 boolean 두지 않고 `status != PENDING`으로 판단** (단일 진실 원천).

### 2-3. Report 엔티티 변경
- `reason: String` → `reason: ReportReason` (`@Enumerated(EnumType.STRING)`)
- `targetId: Long` **유지** — 단, 저장 값은 항상 **내부 정규화 id**
  - `POST` → board.id, `REPLY` → boardReply.id, `USER` → boardMember.id
- 처리 추적 컬럼 (이미 일부 있음, 하나 추가):
  | 컬럼 | 용도 | 비고 |
  |------|------|------|
  | `status` | 처리 여부 + 결과 | `PENDING`=미처리. 기존 有 |
  | `reviewedAt: LocalDateTime?` | 처리 시각 | 미처리 시 null. 기존 有 |
  | `adminMemo: String?` | 처리 메모 | 기존 有 |
  | `reviewedBy: String?` | **처리한 관리자 email** | **신규**. 운영자 늘어도 이력 추적 가능 |
  - 1인 운영이면 `reviewedBy`는 지금 생략 가능 — 단, 컬럼만 미리 넣어두면 나중에 마이그레이션 안 해도 됨 (권장).
- 중복 방지용 유니크 제약 추가:
  ```kotlin
  @Table(
      name = "report",
      uniqueConstraints = [UniqueConstraint(
          name = "uk_report_reporter_target",
          columnNames = ["reporter_email", "target_type", "target_id"]
      )]
  )
  ```
  → 같은 사람이 같은 대상을 두 번 신고 못 함 (DB 레벨 backstop).
  트레이드오프: 반려(REJECTED) 후 재신고도 막힘. MVP에선 단순함 우선, 재신고 필요해지면 그때 완화.
  ⚠️ ddl-auto=update는 **이미 존재하는 테이블에 유니크 제약을 자동 추가하지 않을 수 있음** → 13번 참고.

## 3. API 명세

### 요청 `POST /report`
```jsonc
{
  "target_type": "POST",      // POST | REPLY | USER
  "target_id": "aB3xY",        // POST/REPLY: hashids string, USER: 대상 email
  "reason": "SPAM",            // ReportReason
  "description": "광고 도배글" // ETC면 필수, 그 외 선택 (max 500)
}
```

### DTO 변경 (`ReportCreateRequest`)
```kotlin
data class ReportCreateRequest(
    @field:NotNull
    @JsonProperty("target_type") val targetType: ReportTargetType,

    @field:NotBlank
    @JsonProperty("target_id") val targetId: String,   // Long → String

    @field:NotNull
    val reason: ReportReason,                            // String → enum

    @field:Size(max = 500)
    val description: String? = null,
)
```

### 응답
- 성공: `CommonResponse(code = 201)` (기존 유지)
- 실패: 아래 ErrorCode 기반 `CommonResponse`

## 4. 검증 규칙 (ReportService에서 처리)

순서대로:

1. **사유 검증** — `reason == ETC && description.isNullOrBlank()` → `REPORT_DESCRIPTION_REQUIRED`
2. **대상 해석 + 존재 검증** (targetType 분기):
   | type | 식별자 해석 | 존재/유효 검증 | 정규화 id |
   |------|-----------|--------------|----------|
   | POST | hashids decode | `findBoardById` && `del == false` | board.id |
   | REPLY | hashids decode | `findById` && `del != true` | reply.id |
   | USER | email 그대로 | `findByEmail` && `status == ACTIVE` | member.id |
   - 없거나 삭제/탈퇴 → `REPORT_TARGET_NOT_FOUND`
3. **자기 신고 방지**:
   - POST → `board.email.email == reporter`
   - REPLY → `reply.email == reporter`
   - USER → `targetEmail == reporter`
   - → `REPORT_SELF_NOT_ALLOWED`
4. **중복 신고 방지** — `existsByReporterEmailAndTargetTypeAndTargetId(...)` → `REPORT_ALREADY_EXISTS`
5. 통과 시 `Report.create(...)` 저장 (정규화된 Long id로).

> 자기 신고 검증은 대상 해석 단계에서 이미 작성자/대상 정보를 들고 있으므로, 묶어서 처리하면 쿼리 추가 없음.

## 5. Repository 메서드 (신규)

```kotlin
interface ReportRepository : JpaRepository<Report, Long> {
    fun existsByReporterEmailAndTargetTypeAndTargetId(
        reporterEmail: String,
        targetType: ReportTargetType,
        targetId: Long,
    ): Boolean
}
```

## 6. ErrorCode 추가 (`web-common`)

```kotlin
REPORT_TARGET_NOT_FOUND(message = "신고 대상을 찾을 수 없습니다."),
REPORT_SELF_NOT_ALLOWED(message = "자기 자신은 신고할 수 없습니다."),
REPORT_ALREADY_EXISTS(message = "이미 신고한 대상입니다."),
REPORT_DESCRIPTION_REQUIRED(message = "기타 사유는 상세 내용이 필요합니다."),
```

Exception은 기존 패턴(`CustomException` 상속)을 그대로 따른다. `WebExceptionHandler`가
`CustomException`을 잡아 `code`/`status`로 응답을 만든다:

```kotlin
// web-common/.../exception/WebExceptionHandler.kt 에 추가
class ReportException(
    message: String?,
    code: Int? = null,
    status: HttpStatus? = null,
) : CustomException(message, code, status)
```
- `code`/`status`를 안 넘기면 기존 컨벤션대로 **HTTP 200 OK + body `code=400`**로 응답된다
  (앱은 body의 `code`를 보고 처리). 즉 신고 실패도 HTTP 상태는 200이고 메시지로 구분.

## 7. ReportService 구조 (스케치)

```kotlin
@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val boardRepository: BoardRepository,
    private val boardReplyRepository: BoardReplyRepository,
    private val boardMemberRepository: BoardMemberRepository,
    private val hashidsUtil: HashidsUtil,
) {
    @Transactional
    fun saveReport(userInfo: CommonUserInfo, req: ReportCreateRequest) {
        // 1. 사유 검증
        if (req.reason == ReportReason.ETC && req.description.isNullOrBlank())
            throw ReportException(ErrorCode.REPORT_DESCRIPTION_REQUIRED.message)

        // 2~3. 대상 해석 + 존재 + 자기신고 검증 → 정규화 id
        val targetId = resolveAndValidateTarget(userInfo.email, req)

        // 4. 중복 방지
        if (reportRepository.existsByReporterEmailAndTargetTypeAndTargetId(
                userInfo.email, req.targetType, targetId))
            throw ReportException(ErrorCode.REPORT_ALREADY_EXISTS.message)

        // 5. 저장
        reportRepository.save(
            Report.create(userInfo.email, req.targetType, targetId, req.reason, req.description)
        )
    }

    private fun resolveAndValidateTarget(reporter: String, req: ReportCreateRequest): Long {
        return when (req.targetType) {
            ReportTargetType.POST -> {
                val board = boardRepository.findBoardById(hashidsUtil.decode(req.targetId))
                    ?.takeIf { !it.del } ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                if (board.email.email == reporter) throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                board.id!!
            }
            ReportTargetType.REPLY -> {
                val reply = boardReplyRepository.findById(hashidsUtil.decode(req.targetId))
                    .orElse(null)?.takeIf { it.del != true }
                    ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                if (reply.email == reporter) throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                reply.id!!
            }
            ReportTargetType.USER -> {
                if (req.targetId == reporter) throw ReportException(ErrorCode.REPORT_SELF_NOT_ALLOWED.message)
                val member = boardMemberRepository.findByEmail(req.targetId)
                    ?.takeIf { it.status == UserStatus.ACTIVE }
                    ?: throw ReportException(ErrorCode.REPORT_TARGET_NOT_FOUND.message)
                member.id!!
            }
        }
    }
}
```

## 8. 변경 파일 목록 (구현 시)

| 파일 | 작업 |
|------|------|
| `common/.../enums/report/ReportReason.kt` | 신규 |
| `common/.../enums/report/ReportStatus.kt` | 상태값 정비 (RESOLVED/REJECTED) |
| `board/.../domain/report/Report.kt` | reason 타입, unique 제약, reviewedBy 추가 |
| `board/.../domain/report/ReportRepository.kt` | exists 메서드 |
| `board/.../controller/report/dto/ReportCreateRequest.kt` | targetId String, reason enum, validation |
| `board/.../service/report/ReportService.kt` | 검증 로직 전면 |
| `web-common/.../exception/ErrorCode.kt` | 코드 4개 |
| `web-common/.../exception/` | ReportException 추가 |

## 9. 의도적으로 뺀 것 (오버엔지니어링 경계)
- 자동 블라인드 / 신고 누적 카운트 집계 → 수동 검토 정책이라 불필요
- 어드민 조회·처리 API → 다음 작업 (admin 모듈 연동)
- 신고 알림(FCM) → 운영자 알림 필요해지면 추가
- 재신고 허용(반려 후) → 운영하며 필요성 보고 결정

## 10. 앱 연동 메모
- `PostInfo`가 작성자 `email`을 내려주고 있어 USER 신고에 그대로 사용 가능.
  단, **email 노출 자체가 개인정보 이슈**가 될 수 있으니 중장기적으로는 닉네임/식별 토큰으로 가리는 걸 권장.
- 게시글/댓글 id는 이미 hashids string으로 내려가므로 앱은 그대로 `target_id`에 실어 보내면 됨.

---

# 11. show 컬럼 리팩토링 + 블락 모델

> 배경: 앱에서 "지도 노출" 설정이 제거됨 → `map_show` 컬럼 불필요.
> `map_show` 제거하고 `show` 단일 컬럼을 **노출 상태머신**으로 사용. 관리자 블락도 이 상태에 흡수.

## 11-1. PostShow enum 변경
`board/.../enums/PostShow.kt`

```kotlin
enum class PostShow(val description: String) {
    PUBLIC("전체 공개"),
    PRIVATE("감춤"),        // 작성자 비공개
    FRIENDS("친구만 공개"),  // 추후 팔로잉 기능에서 활용
    BLOCKED("관리자 블락"),   // 관리자 신고 처리
}
// SAME 제거
```
- `MapShow.kt` enum은 현재 타입 참조처가 없는 **죽은 코드** → 제거.

## 11-2. 설계 판단: 블락을 왜 별도 컬럼이 아닌 show에 넣나
- `del`(삭제됨)과 블락(숨김)은 생명주기가 다른 상태 → 분리해야 함 (앞 9번 참고).
- 반면 `show`는 "노출 상태" 축이고 `BLOCKED`도 "강제 비노출"이라 같은 축 → **통합이 자연스러움**.
- 트레이드오프: 블락 시 작성자 원래 값(PUBLIC/PRIVATE/FRIENDS)이 덮임.
  → **신고 대상은 본질적으로 PUBLIC**(비공개 글은 타인이 못 봐 신고 불가)이므로
  **언블락 = PUBLIC 복구**로 단순화. "이전 상태 저장 컬럼" 만들지 않음 (오버엔지니어링 회피).

## 11-3. 블락 동작 (어드민 처리 API 범위 — 다음 작업)
- 신고 처리 `ReportStatus.RESOLVED` 시: 대상 게시글 `show = BLOCKED`.
- 언블락(오신고 복구): `show = PUBLIC`.
- 댓글 블락은 별도 정책 필요 (BoardReply엔 show 없음) → 댓글은 `del` 또는 댓글 전용 플래그.
  이번 모델 범위는 **게시글 블락**까지. 댓글 블락은 어드민 작업 때 결정.

## 11-4. 단건 조회 가시성 가드 (중요)
현재 `getPost()`는 `show`/`del`을 **안 거름** → 블락/비공개 글도 id로 직접 열림. 블락이 반쪽이 됨.
- 비소유자: `show in (PRIVATE, BLOCKED)` 또는 `del=true` → 기존 패턴대로 `BoardNotFoundException(ErrorCode.BOARD_NOT_FOUND.message)` throw.
  (이 컨벤션은 HTTP 404가 아니라 **HTTP 200 + body code=400**임에 유의 — 6번 참고. 실제 404를 만들지 말 것.)
- 소유자: 본인 글은 PRIVATE까지 조회 허용. BLOCKED는 "블락됨" 안내 노출 여부 정책 결정 필요.
- 댓글 native 쿼리(`getBoardReplyByBoardId`)도 `del` 필터 누락 → 함께 점검.

---

# 12. 마이그레이션 & 배포 전략 (구버전 호환 불필요 — 한 번에 전환)

## 12-1. 실측 결론: 왜 단계적 롤아웃이 필요 없나
Flutter 코드 실측 결과, 구버전 앱이 `mapShow`/`show` 변경에 **깨지지 않음**:

| 항목 | 실측 내용 | 결론 |
|------|----------|------|
| `BoardPost.fromJson` | `json['mapShow'] as String? ?? ''` (null-safe) | 응답에서 `mapShow` 빼도 `''`로 받음 → **크래시 없음** |
| `show` 처리 | enum 파싱 아님, `show == 'PUBLIC'` 문자열 비교만 | `BLOCKED`/`FRIENDS` 모르는 값 와도 **안전** (지도탭 비활성 정도) |
| `mapShow` 사용처 | 모델 필드/직렬화에만 존재, **읽는 로직 0** | 이미 죽은 값 → 제거 무영향 |
| 앱→서버 전송 | 생성=`isShow`만, 수정=`isShow` + `isMapShow:'SAME'`(하드코딩 1곳) | `isMapShow`는 unknown으로 무시되면 끝 |

→ **응답 `mapShow` 즉시 제거 + `BLOCKED` 추가 + `SAME` 제거**, 전부 한 번에 가도 됨.
더미 응답·3-Phase·강제 업데이트 게이팅 **불필요**.

## 12-2. 백엔드 변경 (1회 배포)
- `PostShow`: `SAME` 제거, `BLOCKED` 추가 (11-1).
- `Board`: `mapShow` 필드 제거.
- `PostInfo`: `mapShow` 필드 제거.
- `PostCreateRequest`/`PostUpdateRequest`: `isMapShow` 제거.
- `PostService`: `mapShow` 계산 블록 전부 제거 + 단건조회 가시성 가드 추가 (11-4).
- `MapBoardRepositoryImpl`: `map_show` 조건(68~70줄) → `b.show = 'PUBLIC'` 하나로.
- **유일한 안전장치**: 구버전 수정요청이 보내는 `isMapShow:'SAME'`을 거부하지 않도록
  Jackson `FAIL_ON_UNKNOWN_PROPERTIES=false` 확인 (Spring 기본값이지만 명시 점검).
  DTO에서 `isMapShow`를 빼도 unknown 필드로 무시되므로 OK.

## 12-3. DB 마이그레이션 (무중단 안전 순서)
`map_show`가 `NOT NULL`이라, 엔티티에서 필드만 빼고 컬럼을 그대로 두면
**신규 INSERT가 NOT NULL 위반으로 실패**함. 그래서 순서가 중요:

```sql
-- 1) 먼저 NOT NULL 해제 (이 시점부터 컬럼 없이 INSERT 해도 NULL 허용 → 안전)
ALTER TABLE board ALTER COLUMN map_show DROP NOT NULL;
-- 2) 신규 코드(엔티티에 mapShow 없음) 배포
-- 3) 안정화 후 정리 (언제든)
ALTER TABLE board DROP COLUMN map_show;
```
- `show` 컬럼에 `SAME` 값 잔존 사전 점검:
  `SELECT count(*) FROM board WHERE show = 'SAME';` → 있으면 `UPDATE ... SET show='PUBLIC'` 후 enum 정리.
  (단 `show` 기본값이 PUBLIC이라 실제 잔존 가능성은 낮음)

## 12-4. 영향 파일 — 백엔드
| 파일 | 작업 |
|------|------|
| `enums/PostShow.kt` | `SAME` 제거, `BLOCKED` 추가 |
| `enums/MapShow.kt` | 삭제 (죽은 코드) |
| `domain/board/Board.kt` | `mapShow`/`map_show` 제거 |
| `domain/board/map/MapBoardRepositoryImpl.kt` | `map_show` 조건 → `b.show='PUBLIC'` |
| `controller/post/dto/PostCreateRequest.kt` | `isMapShow` + `mapShow=isShow` 매핑 제거 |
| `controller/post/dto/PostUpdateRequest.kt` | `isMapShow` 제거 |
| `service/post/dto/PostInfo.kt` | `mapShow` 필드 제거 |
| `service/post/PostService.kt` | mapShow 계산 블록 제거, 단건조회 가시성 가드 추가 |
| DB 마이그레이션 | `map_show` NOT NULL 해제 → DROP |

## 12-5. 영향 파일 — Flutter (`unimal_flutter`, 별도 repo / 백엔드와 독립 진행 가능)
| 위치 | 작업 | 비고 |
|------|------|------|
| `service/board/model/board_post.dart` | `mapShow` 필드 제거 | 안 지워도 `''`로 무해 — 정리 차원 |
| `service/board/board_api_service.dart` (updateBoard) | `isMapShow: 'SAME'` 라인 제거 | 서버가 무시하므로 급하지 않음 |
| `screens/.../detail_board_card.dart` 등 `show=='PUBLIC'` 비교 | 그대로 유지 | PRIVATE/BLOCKED는 지도탭 비활성으로 자연 처리 |
| `show` 이진 토글 UI | 그대로 | FRIENDS 추가는 팔로잉 기능 때 |

> 앱은 백엔드 배포에 의존하지 않음. 응답 `mapShow`가 사라져도 `''`로 받고 안 쓰므로,
> 앱 수정은 **여유 있을 때** 정리하면 됨.

## 12-6. 회귀 테스트 체크리스트
- 게시글 리스트/내글/지도 마커가 BLOCKED 글을 노출하지 않는지
- PRIVATE/BLOCKED 글 단건 id 직접조회 시 비소유자 404
- 게시글 생성/수정 정상 (특히 `isMapShow` 없이도, 그리고 구버전이 보내도)
- 신고 RESOLVED → show=BLOCKED → 리스트/지도/단건 전부에서 사라지는지
- 언블락 → PUBLIC 복구 후 정상 노출
- 신규 게시글 INSERT 시 `map_show` NOT NULL 위반 안 나는지 (마이그레이션 순서 검증)

---

# 13. 클로드코드 인계 체크포인트 (코드 검증 결과)

> 실제 코드와 대조해 확인한 사실 + 자동화로 안 잡히는 함정. 구현 전 반드시 읽을 것.

## 13-1. 스키마 관리 = `ddl-auto: update` (Flyway/Liquibase 없음)
모든 서비스가 `spring.jpa.hibernate.ddl-auto: update`. 마이그레이션 도구 없음. 의미:
- **컬럼/테이블 추가는 자동** → `report.reviewed_by` 추가, `report` 테이블 생성은 앱 기동 시 자동 반영.
- **컬럼 DROP·타입 변경·NOT NULL 변경은 절대 자동 안 됨** → `board.map_show` 제거는 **수동 SQL 필수** (12-3).
- **유니크 제약**(`uk_report_reporter_target`)은 테이블 신규 생성 시엔 반영되지만, **이미 존재하는 `report` 테이블엔 자동 추가 안 될 수 있음** → 아래 SQL 수동 실행 권장:
  ```sql
  ALTER TABLE unimal_board.report
    ADD CONSTRAINT uk_report_reporter_target UNIQUE (reporter_email, target_type, target_id);
  ```
  (중복 데이터 있으면 먼저 정리 후 추가)

## 13-2. enum 값 제거 시 기존 데이터 점검
`ReportStatus`에서 `REVIEWED/DISMISSED/SUCCESSED` 제거, `PostShow`에서 `SAME` 제거 →
`@Enumerated(STRING)`이라 **DB에 해당 문자열 row가 있으면 조회 시 IllegalArgumentException**.
```sql
SELECT count(*) FROM unimal_board.report WHERE status IN ('REVIEWED','DISMISSED','SUCCESSED');
SELECT count(*) FROM unimal_board.board  WHERE show = 'SAME';
```
- `report`는 신규 기능이라 보통 0건. `board.show`도 기본값 PUBLIC이라 0건일 가능성 높음.
- 있으면 `UPDATE`로 신규 값에 매핑(예: status→PENDING, show→PUBLIC) 후 enum 정리.

## 13-3. 공통 enum 변경 → admin 모듈 파급
`ReportStatus`/`ReportReason`은 `common`에 있고 **`admin` 모듈도 `Report` 엔티티로 같은 테이블을 매핑**한다
(`admin/.../domain/report/Report.kt`, `@Table(schema="unimal_board")`).
- 다행히 admin·board 둘 다 `ReportStatus.PENDING`만 참조 → **값 제거로 컴파일 깨지지 않음**.
- 단 `common` 변경 시 **admin 모듈도 재빌드 필요**. (CI 규칙엔 admin 트리거가 명시 안 됨 — 수동 배포 확인)
- admin의 `Report.reason`은 아직 `String`. board만 `ReportReason` enum으로 바꿈 → DB 컬럼은 varchar 그대로라 호환 OK. admin 쪽 enum 통일은 어드민 작업 때.

## 13-4. hashids decode 동작
`HashidsUtil.decode()`는 **잘못된 문자열이면 `HashidsException`(HASHIDS_DECODE_ERROR)을 throw**한다
(`REPORT_TARGET_NOT_FOUND` 아님). 즉 POST/REPLY 신고 시 `target_id`가 깨진 값이면 그 에러로 응답됨.
- 정상 디코드됐지만 존재하지 않는 id → `findBoardById`가 null → `REPORT_TARGET_NOT_FOUND`. (정상 흐름)
- 클라이언트엔 둘 다 "대상 없음"으로 보여도 무방. 별도 처리 불필요.

## 13-5. 커밋 규칙 (CLAUDE.md)
- **중간 커밋 금지.** 신고 접수 기능 **전부 끝난 뒤(빌드/테스트 통과) 한 번에 커밋.**
- 사용자가 명시 요청 전엔 커밋하지 말 것.

## 13-6. 이번 작업 범위 = 신고 접수만 (재확인)
- 구현 대상: 1~7번(신고 접수 검증/저장) + 11번 중 **모델/enum/단건 가시성 가드**까지.
- **블락 실행(show=BLOCKED 세팅)·신고 목록/처리는 admin 작업** → 이번 PR에 넣지 말 것.
- `show` 리팩토링(map_show 제거)은 신고와 독립적이나 같은 board 모듈이라 함께 진행 가능 —
  단 12-3 마이그레이션 순서(컬럼 NOT NULL 해제→배포→DROP)를 반드시 지킬 것.
