# Signup V2 API & Controller Split 설계

## 배경

iOS/Android 배포 시점이 다를 수 있으므로, 구버전 앱(`/signup/manual`)은 그대로 유지하고
신버전 앱용 `/signup/manual/v2`를 신규 추가한다.

신버전 회원가입은 소셜 로그인의 tel-missing 플로우와 동일한 방식을 따른다.

---

## 플로우 비교

### 구버전 (V1) — 유지, 변경 없음

```
email/code-request → email/code-verify
tel/code-request   → tel/code-verify
POST /auth/signup/manual  { nickname, email, password, checkPassword, tel }
→ emailTelSuccessCheck(email, tel) 통과 → 계정 생성 → 완료
```

### 신버전 (V2) — 소셜 로그인 tel 플로우와 동일

```
Step 1: email/code-request → email/code-verify
Step 2: POST /auth/signup/manual/v2  { nickname, email, password, checkPassword }
         → emailSuccessCheck(email) 통과 → 계정 생성(tel=null)
         → TelNotFoundException(code=1009, data=email) 발생
         → Flutter: code 1009 감지 → tel 입력 화면 (소셜 로그인과 동일 화면)
Step 3: POST /auth/email-tel/code-request  { email, tel }  ← 기존 재활용
Step 4: POST /auth/tel/check-update        { email, tel, code }  ← 기존 재활용
         → tel 업데이트 + JWT 발급
```

---

## 핵심 설계 결정

- **v2 signup은 항상 TelNotFoundException을 throw한다.** 계정 생성 성공 = code 1009 응답.
  Flutter는 이미 소셜 로그인에서 1009를 tel 화면 트리거로 처리하므로 클라이언트 변경 최소화.
- **Step 3~4는 기존 엔드포인트 그대로 재활용.** 신규 엔드포인트 없음.
- **email 인증 검증만 수행** (`"${email}:auth-code"` = SUCCESS). tel 검증은 step 4에서.

---

## 신규 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | `/auth/signup/manual/v2` | 신버전 회원가입 (tel 없이) | 불필요 |

### Request Body

```kotlin
data class SignupV2Request(
    @field:NotBlank val nickname: String,
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val checkPassword: String,
)
```

### Response (항상 이 형태)

```json
{ "code": 1009, "message": "휴대폰 번호가 존재하지 않습니다.", "data": "user@example.com" }
```

---

## 컨트롤러 분리

현재 `AuthController` (15개 엔드포인트) → 3개로 분리

| 컨트롤러 | 경로 | 포함 엔드포인트 |
|---------|------|--------------|
| `SignUpController` | `/auth/signup/*` | v1 signup, v2 signup |
| `LoginController` | `/auth/login/*`, `/auth/logout`, `/auth/withdrawal`, `/auth/token-reissue`, `/auth/tel/check-update` | 모든 로그인 + 세션 |
| `AuthenticationController` | `/auth/email/*`, `/auth/tel/code-request`, `/auth/tel/code-verify`, `/auth/email-tel/*` | 인증코드 발송/검증 |

`AuthController.kt` 삭제.

---

## 변경 파일 목록

| 작업 | 파일 |
|------|------|
| 신규 생성 | `controller/SignUpController.kt` |
| 신규 생성 | `controller/LoginController.kt` |
| 신규 생성 | `controller/AuthenticationController.kt` |
| 신규 생성 | `controller/request/SignupV2Request.kt` |
| 수정 | `service/login/LoginService.kt` (signupV2 함수 추가) |
| 삭제 | `controller/AuthController.kt` |

---

## 검증 포인트

- v1 `/signup/manual` 동작 그대로 유지 (회귀 없음)
- v2 signup 후 `tel/check-update` 호출 시 JWT 정상 발급
- v2 signup 시 이메일 중복/닉네임 중복 에러 정상 동작
- email 인증 미완료 시 `AUTHENTICATION_NOT_COMPLETED` 에러 반환
