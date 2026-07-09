# Admin Thymeleaf 관리자 페이지 설계

## 배경

`admin.unimal.co.kr`은 API Gateway를 거치지 않고 nginx에서 `admin-server`로 직접 라우팅한다.
관리자 화면은 Thymeleaf 서버 렌더링으로 제공하고, 인증은 user 모듈 JWT가 아니라 admin 모듈의 Spring Security 세션 로그인을 사용한다.

관리자 계정은 실제 앱 사용자와 분리한다. `admin_member`, `admin_member_role`은 `unimal_admin` 스키마가 소유하고, 신고/게시글 같은 운영 대상 데이터는 기존 도메인 스키마를 읽는다.

---

## 핵심 설계 결정

- **1차 화면은 앱 회원 목록으로 시작한다.** 실제 서비스 회원 테이블은 `unimal_user.member`에 그대로 두고, admin 모듈에서는 read-only 운영 조회 화면으로 제공한다.
- **신고 처리 큐와 대시보드는 2차로 붙인다.** 회원 목록, 로그인, 권한 관리가 안정된 뒤 신고 처리와 홈 지표를 확장한다.
- **admin 인증은 세션 기반이다.** 브라우저는 `JSESSIONID` 쿠키를 사용하고, admin 내부 JSON API도 같은 세션으로 보호한다.
- **관리자 계정은 별도 테이블을 사용한다.** 앱 사용자 `member`와 섞지 않는다.
- **공식 진입점은 `admin.unimal.co.kr` 하나로 둔다.** API Gateway의 admin route는 사용하지 않는다.

---

## URL 구조

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/login` | 관리자 로그인 화면 | 불필요 |
| POST | `/login` | Spring Security form login 처리 | 불필요 |
| POST | `/logout` | 관리자 로그아웃 | 필요 |
| GET | `/` | 1차에서는 `/members`로 redirect, 2차에서 홈 대시보드 제공 | 필요 |
| GET | `/members` | 앱 회원 목록 화면 | 필요 |
| GET | `/reports` | 신고 처리 큐 화면 | 필요 |
| GET | `/reports/{id}` | 신고 상세 화면 | 필요 |
| POST | `/reports/{id}/approve` | 신고 승인/조치 처리 | 필요 |
| POST | `/reports/{id}/dismiss` | 신고 반려 처리 | 필요 |
| GET | `/admin-members` | 관리자 계정 목록 | `SUPER_ADMIN` |

1차는 Thymeleaf 화면과 form POST/redirect로 구현한다. 내부 JSON API가 필요해지는 2차부터 `/api/**`를 사용하고, `/api/**`도 동일한 세션 인증과 CSRF 정책을 따른다.

---

## 화면 구성

### 1차: 앱 회원 목록

좌측에는 고정 사이드바를 둔다.

```
Stomap Admin
- 회원 관리
- 신고 관리
- 게시글 관리
- 관리자 계정
```

메인 영역은 서비스 회원의 운영 조회 테이블을 보여준다.

```
회원 목록 테이블
- 회원 ID
- 닉네임/이름
- 이메일
- 가입 방식
- 상태
- 가입일
- 수정일

[이전] [현재 페이지] [다음]
```

목표는 관리자 로그인 직후 실제 서비스 회원 상태를 바로 확인하는 것이다. `member` 테이블은 user 모듈 소유이므로 admin 모듈에서는 조회용 엔티티와 서비스만 둔다.

### 2차: 신고 처리 큐와 홈 대시보드

신고 처리 큐는 목록과 상세를 동시에 보여주는 화면으로 확장한다.

```
[상태 필터] [유형 필터] [검색]

신고 목록 테이블
- 상태
- 대상 유형
- 신고 사유
- 신고자
- 생성 시간

우측 상세 패널
- 대상 미리보기
- 신고 내용
- 관리자 메모
- 반려 버튼
- 숨김/처리 버튼
```

홈 화면은 운영 현황 요약으로 확장한다.

```
상단 지표
- 대기 신고 수
- 오늘 처리 수
- 숨김 처리 수
- 관리자 로그인 실패/잠금 수

중앙
- 긴급 처리 큐
- 최근 처리 이력
- 신고 유형별 추이
```

대시보드의 상세 진입은 `/reports`로 연결한다.

---

## 도메인 모델

### `admin_member`

| 컬럼 | 설명 |
|------|------|
| `id` | 자동 증가 PK |
| `login_id` | 관리자 로그인 아이디 |
| `password` | BCrypt 해시 |
| `name` | 관리자 이름 |
| `email` | 연락용 이메일 |
| `status` | `ACTIVE`, `INACTIVE`, `LOCKED`, `RESIGN` |
| `last_login_at` | 마지막 로그인 시각 |

### `admin_member_role`

| 컬럼 | 설명 |
|------|------|
| `id` | 자동 증가 PK |
| `admin_member_id` | `admin_member.id` FK |
| `role` | `ADMIN`, `SUPER_ADMIN` |

`admin_member_id + role` 조합은 unique로 둔다.

### `unimal_user.member`

admin 모듈에서는 `AppMember` 조회용 엔티티로 매핑한다.

| 컬럼 | 설명 |
|------|------|
| `id` | 앱 회원 PK |
| `nickname`, `name` | 회원 표시명 |
| `email` | 회원 이메일 |
| `provider` | 가입 방식 |
| `status` | `ACTIVE`, `INACTIVE`, `BLOCK`, `WITHDRAWAL`, `RESIGNIN` |
| `created_at`, `updated_at` | 가입/수정 시각 |

---

## 패키지 구조

```
admin/src/main/kotlin/com/unimal/admin/
├── config/
│   ├── SecurityConfig.kt
│   └── QueryDslConfig.kt
├── controller/
│   ├── view/
│   │   ├── LoginViewController.kt
│   │   ├── appmember/
│   │   │   └── AppMemberViewController.kt
│   │   ├── DashboardViewController.kt
│   │   └── ReportViewController.kt
│   └── api/
│       └── ReportAdminApiController.kt
├── domain/
│   ├── adminmember/
│   ├── appmember/
│   └── report/
└── service/
    ├── adminmember/
    ├── appmember/
    └── report/
```

Thymeleaf 템플릿은 다음처럼 둔다.

```
admin/src/main/resources/templates/
├── layout/
│   └── admin-layout.html
├── login.html
├── dashboard.html
├── appmember/
│   └── list.html
└── reports/
    ├── list.html
    └── detail.html
```

정적 리소스는 `admin/src/main/resources/static/admin/` 아래에 둔다.

---

## 인증과 권한

`AdminUserDetailsService`가 `admin_member.login_id`로 계정을 조회한다.

로그인 허용 조건:

- `status = ACTIVE`
- 비밀번호 BCrypt 검증 성공
- `ADMIN` 또는 `SUPER_ADMIN` 권한 보유

권한 정책:

| 권한 | 가능 작업 |
|------|----------|
| `ADMIN` | 신고 조회, 신고 처리, 게시글 운영 처리 |
| `SUPER_ADMIN` | `ADMIN` 권한 + 관리자 계정 관리 |

로그인 실패, 비활성, 잠금 계정은 로그인 화면에 일반 오류로 표시한다. 계정 존재 여부가 드러나는 상세 메시지는 노출하지 않는다.

---

## 오류 처리

- 신고가 이미 처리된 상태에서 다시 처리하면 화면에 충돌 메시지를 보여준다.
- 관리자 메모 길이 제한을 초과하면 같은 화면에서 validation 메시지를 보여준다.
- 세션 만료 시 `/login?expired`로 이동한다.
- 권한 부족 시 전용 403 화면을 보여준다.

---

## 검증 포인트

- `/login`에서 정상 관리자 계정으로 로그인하면 `/members`로 이동한다.
- `/`는 `/members`로 이동한다.
- `/members`는 앱 회원 목록을 최신 가입순 페이지로 보여준다.
- 비활성/잠금/권한 없는 계정은 로그인할 수 없다.
- `ADMIN`은 신고 처리 화면에 접근할 수 있다.
- `ADMIN`은 관리자 계정 관리 화면에 접근할 수 없다.
- `SUPER_ADMIN`은 관리자 계정 관리 화면에 접근할 수 있다.
- 신고 목록은 상태/유형/검색 필터로 조회된다.
- 신고 승인/반려 처리 후 목록과 상세 상태가 갱신된다.
- CSRF 토큰이 없는 POST 요청은 거부된다.
