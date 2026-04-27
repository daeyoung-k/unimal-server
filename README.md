# 🗺️ Unimal Server - 스토맵 Backend

> **지도 위에 일상의 이야기를 핀으로 남기고 주변 사람들과 공유하는 위치 기반 스토리 앱, 스토맵(Stomap)의 멀티모듈 마이크로서비스 백엔드**

![Kotlin](https://img.shields.io/badge/Kotlin_1.9.25-7F52FF?style=flat&logo=Kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.3-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC_1.72-4285F4?style=flat&logo=grpc&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_+_PostGIS-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white)

## 📋 프로젝트 개요

Flutter 기반 iOS/Android 앱 **스토맵(Stomap)** 의 백엔드 시스템입니다.
사용자가 현재 위치에서 사진과 글을 게시하면 지도 마커로 노출되어 주변 사람들과 공유됩니다.
마이크로서비스 아키텍처로 각 도메인이 독립적으로 배포·확장됩니다.

### 🎯 핵심 기능
- **사용자 인증/인가** — JWT + OAuth2 (Kakao, Naver, Google) 기반 보안 시스템
- **위치 기반 게시물** — PostGIS 공간 필터링 + 복합 점수 정렬 마커 제공
- **게시판 시스템** — 게시글, 댓글, 좋아요 CRUD
- **사진 관리** — AWS S3 + CloudFront CDN 연동
- **앱 푸시 알림** — Firebase Admin SDK (FCM) 기반
- **이메일/SMS 인증** — Gmail SMTP + Naver Cloud SMS

---

## 🏗️ 시스템 아키텍처

### 멀티모듈 마이크로서비스 구조

```
unimal-server/
├── api-gateway/       # API 게이트웨이 (포트: 8080) — 단일 진입점, JWT 인증
├── user/              # 사용자 서비스   (포트: 8081, gRPC: 50081, context-path: /user)
├── board/             # 게시판 서비스   (포트: 8083, gRPC: 50083, context-path: /board)
├── map/               # 지도 서비스     (포트: 8082, gRPC: 50082, context-path: /map)
├── photo/             # 사진 서비스     (포트: 8084, gRPC: 50084, context-path: /photo)
├── notification/      # 알림 서비스     (포트: 8085, gRPC: 50085, context-path: /notification)
├── common/            # 공통 유틸리티 (QueryDSL, Hashids, CommonResponse, Kafka DTO)
├── web-common/        # 웹 공통 컴포넌트 (JWT 유틸, UserInfo 어노테이션)
└── proto-common/      # gRPC Protocol Buffers 정의
```

> 각 서비스는 `context-path`로 자신의 prefix를 가집니다.
> 게이트웨이가 `/user/**` → user 서비스(8081)로 라우팅하면, 서비스 내부에서는 `/auth/**`, `/member/**` 로 처리합니다.

### 통신 아키텍처

| 통신 방향 | 방식 | 설명 |
|---------|------|------|
| Flutter 앱 → Backend | REST / HTTP | API Gateway(8080)를 단일 진입점으로 사용 |
| Service ↔ Service | gRPC | 타입 안전한 동기 내부 통신 |
| 이벤트 기반 | Apache Kafka | 비동기 이벤트 처리 (알림, 카운트 집계 등) |

### API Gateway 인증 필터 3종류

| 필터 | 적용 라우트 | 동작 |
|------|-----------|------|
| `AccessTokenFilter` | 인증 필수 API | Access Token 검증, 사용자 정보 헤더 주입 |
| `RefreshTokenFilter` | 토큰 재발급·로그아웃·탈퇴 | Refresh Token DB 검증 후 처리 |
| `OptionalAccessTokenFilter` | 인증 선택 API (게시글 조회 등) | 토큰 있으면 사용자 정보 주입, 없어도 통과 |

**Header 기반 사용자 정보 전파** (Gateway → 각 서비스):
```
X-Unimal-Email          사용자 이메일
X-Unimal-Access-Token   Access Token
X-Unimal-Refresh-Token  Refresh Token
X-Unimal-Provider       OAuth 제공자 (kakao / naver / google)
```

---

## ⚙️ 기술 스택

### Backend
| 분류 | 기술 | 버전 |
|------|------|------|
| 언어 | Kotlin | 1.9.25 |
| JVM | Java | 21 |
| 프레임워크 | Spring Boot | 3.4.3 |
| 빌드 | Gradle Kotlin DSL | — |
| ORM | Spring Data JPA + Hibernate | 6.6.8.Final |
| 동적 쿼리 | QueryDSL | 5.1.0 |

### 통신
| 분류 | 기술 | 버전 |
|------|------|------|
| API Gateway | Spring Cloud Gateway | 2024.0.0 |
| 내부 통신 | gRPC + Protocol Buffers | 1.72.0 / 4.31.0 |
| 메시지 브로커 | Apache Kafka | 3 브로커 클러스터 |

### 데이터베이스 & 캐시
| 분류 | 기술 | 비고 |
|------|------|------|
| 주 DB | PostgreSQL + PostGIS | 지리 정보 공간 쿼리 |
| 공간 라이브러리 | JTS + Hibernate Spatial | 1.20.0 |
| 캐시/세션 | Redis | 인증코드 임시저장, 세션 관리 |

### 보안 & 인증
| 분류 | 기술 | 비고 |
|------|------|------|
| 토큰 | JWT (JJWT) | 0.12.6, HS256 |
| 소셜 로그인 | OAuth2 | Kakao, Naver, Google |
| 전화 인증 | Naver Cloud SMS | — |
| 이메일 인증 | Gmail SMTP | Thymeleaf 템플릿 |

### 외부 서비스
| 서비스 | 용도 |
|--------|------|
| AWS S3 + CloudFront | 파일 저장 및 CDN (cdn.unimal.co.kr) |
| Firebase Admin SDK 9.7.0 | FCM 앱 푸시 알림 |
| Google Geocoding API | 역지오코딩 (위도/경도 → 주소) |
| Naver Cloud SMS | SMS 인증 코드 발송 |

### 인프라
| 분류 | 기술 |
|------|------|
| 컨테이너 | Docker + Docker Compose |
| CI/CD | GitHub Actions (모듈별 변경 감지 빌드) |
| 모니터링 | Prometheus + Grafana |
| 헬스 체크 | Spring Boot Actuator |

---

## 📡 API 엔드포인트

> 모든 경로는 API Gateway(`api.unimal.co.kr`) 기준이며, Flutter 앱이 호출하는 실제 URL입니다.

### 🔐 User Auth (`/user/auth`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/user/auth/login/mobile/kakao` | 없음 | Kakao 소셜 로그인 |
| POST | `/user/auth/login/mobile/naver` | 없음 | Naver 소셜 로그인 |
| POST | `/user/auth/login/mobile/google` | 없음 | Google 소셜 로그인 |
| POST | `/user/auth/login/manual` | 없음 | 이메일/비밀번호 로그인 |
| POST | `/user/auth/signup/manual` | 없음 | 회원가입 |
| GET | `/user/auth/token-reissue` | Refresh | Access Token 재발급 |
| GET | `/user/auth/logout` | Refresh | 로그아웃 |
| GET | `/user/auth/withdrawal` | Refresh | 회원 탈퇴 |
| POST | `/user/auth/email/code-request` | 없음 | 이메일 인증 코드 발송 |
| POST | `/user/auth/email/code-verify` | 없음 | 이메일 인증 코드 검증 |
| POST | `/user/auth/tel/code-request` | 없음 | 전화번호 인증 코드 발송 |
| POST | `/user/auth/tel/code-verify` | 없음 | 전화번호 인증 코드 검증 |
| POST | `/user/auth/email-tel/code-request` | 없음 | 이메일+전화번호 통합 인증 발송 |
| POST | `/user/auth/email-tel/code-verify` | 없음 | 이메일+전화번호 통합 인증 검증 |
| POST | `/user/auth/tel/check-update` | 없음 | 전화번호 인증 후 토큰 재발급 (응답 헤더에 Provider 포함) |

> 로그인 성공 시 응답 헤더: `X-Unimal-Access-Token`, `X-Unimal-Refresh-Token`, `X-Unimal-Email`, `X-Unimal-Provider`
> Flutter `AccountService`가 이 헤더를 파싱해 `SecureStorage`에 저장합니다.

### 👤 User Member (`/user/member`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/user/member/info` | Access | 사용자 정보 조회 |
| PATCH | `/user/member/info/update` | Access | 사용자 정보 수정 (닉네임, 소개) |
| POST | `/user/member/change/password` | Access | 비밀번호 변경 |
| POST | `/user/member/find/email` | 없음 | 전화번호로 이메일 찾기 |
| POST | `/user/member/find/email-tel/check/request` | 없음 | 비밀번호 재설정 전 이메일+전화번호 확인 |
| POST | `/user/member/find/change/password` | 없음 | 비밀번호 재설정 |
| GET | `/user/member/find/nickname/duplicate` | 없음 | 닉네임 중복 확인 |
| POST | `/user/member/find/email/duplicate` | 없음 | 이메일 중복 확인 + 인증코드 발송 |
| POST | `/user/member/find/tel/duplicate` | 없음 | 전화번호 중복 확인 + 인증코드 발송 |
| POST | `/user/member/device/info/update` | Access | FCM 디바이스 토큰 업데이트 |
| POST | `/user/member/profile/image/upload` | Access | 프로필 이미지 업로드 (Multipart) |

### 📝 Board Post (`/board/post`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/board/post/{boardId}` | Optional | 게시글 상세 조회 |
| GET | `/board/post/list` | Optional | 게시글 목록 (페이징, 검색, 정렬) |
| GET | `/board/post/my/list` | Access | 내 게시글 목록 |
| POST | `/board/post` | Access | 게시글 작성 (Multipart, 사진 최대 10장) |
| PATCH | `/board/post/{boardId}/update` | Access | 게시글 수정 |
| POST | `/board/post/{boardId}/file/upload` | Access | 게시글 파일 추가 업로드 |
| POST | `/board/post/{boardId}/file/delete` | Access | 게시글 파일 삭제 |
| DELETE | `/board/post/{boardId}/delete` | Access | 게시글 삭제 |
| GET | `/board/post/{boardId}/like` | Access | 좋아요 토글 |
| POST | `/board/post/{boardId}/reply` | Access | 댓글 작성 |
| GET | `/board/post/{boardId}/reply` | Optional | 댓글 목록 조회 |
| PATCH | `/board/post/{boardId}/reply/{replyId}/update` | Access | 댓글 수정 |
| DELETE | `/board/post/{boardId}/reply/{replyId}/delete` | Access | 댓글 삭제 |
| GET | `/board/post/total/like` | Access | 내 총 좋아요 수 |
| GET | `/board/post/total` | Access | 내 총 게시글 수 |

### 🗺️ Board Map (`/board/map`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/board/map/location/post` | Access | 줌 레벨 기반 위치 게시물 조회 |

### 📢 Notice (`/board/notice`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/board/notice/{id}` | 없음 | 공지사항 상세 조회 |
| GET | `/board/notice` | 없음 | 공지사항 목록 |
| POST | `/board/notice` | 없음 | 공지사항 작성 |

### 📍 Map (`/map`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| GET | `/map/reverse-geocoding` | 없음 | 역지오코딩 (위도/경도 → 주소) |

### 📸 Photo (`/photo`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/photo/upload` | 없음 | 단일 파일 업로드 (S3) |
| POST | `/photo/multiple-upload` | 없음 | 다중 파일 업로드 (S3) |
| POST | `/photo/delete` | 없음 | 파일 삭제 (S3) |

### 🔔 Notification (`/notification`)

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/notification/app-push` | 없음 | 단일 사용자 FCM 푸시 발송 |
| POST | `/notification/app-push/multicast` | 없음 | 다중 사용자 FCM 푸시 발송 |

---

## 🗺️ 지도 기반 게시물 조회

`GET /board/map/location/post`

Flutter 지도 화면에서 줌 레벨이 변경되거나 지도를 이동할 때 호출됩니다.
사진이 등록된 게시물만 반환하며 복합 점수로 정렬합니다.

### 요청 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `latitude` | Double | 지도 중심 위도 |
| `longitude` | Double | 지도 중심 경도 |
| `zoom` | Int | 줌 레벨 (10 ~ 20), 매핑 없으면 14 기본 |

### 줌 레벨별 조회 범위
| 줌 | 조회 반경 | 최대 게시물 수 |
|----|---------|-------------|
| 10 | 50km | 30 |
| 11 | 30km | 30 |
| 12 | 20km | 30 |
| 13 | 10km | 40 |
| **14** | **5km** | **40 (기본값)** |
| 15 | 3km | 50 |
| 16 | 2km | 50 |
| 17 | 1km | 50 |
| 18 | 500m | 100 |
| 19 | 300m | 100 |
| 20 | 100m | 100 |

### score 계산 알고리즘
```
score = 본인 게시물 보너스 + 신선도 점수 + 참여도 점수

본인 게시물 보너스 : 내 글이면 +10,000 (항상 상단 노출)
신선도 점수       : 30분 이내 +1.0 / 2시간 이내 +0.6 / 6시간 이내 +0.3 / 이후 +0.1
참여도 점수       : 좋아요 × 2 + 댓글 × 3
```

Flutter에서는 `score`를 `globalZIndex = 200000 + score` 로 변환해 마커 우선순위를 결정합니다.

### 구현 상세
- PostGIS `ST_DWithin` + GIST 인덱스로 공간 필터링 O(log n)
- `INNER JOIN LATERAL board_file`로 대표 이미지 1장만 조회, 사진 없는 게시물 제외
- score 계산 및 정렬을 DB 내에서 처리 (애플리케이션 레벨 반복문 없음)

---

## 🔔 앱 푸시 알림 (FCM)

Flutter 앱 `PushNotificationService`가 수신하는 `data` 페이로드 스펙입니다.

### Kafka 이벤트 흐름
```
Board Service (게시글 좋아요/댓글)
  → Kafka: board.postAppPushTopic (PostAppPushEvent)
    → Notification Service Consumer
      → FCM 단건 발송 → Flutter 앱
```

### 알림 타입별 data 페이로드

```json
// 좋아요 알림 → Flutter: 게시글 상세 화면 이동
{ "type": "LIKE", "target_id": "{boardId}" }

// 댓글 알림 → Flutter: 게시글 상세 화면 이동
{ "type": "REPLY", "target_id": "{boardId}" }

// 공지사항 알림 → Flutter: 공지사항 목록 이동
{ "type": "NOTICE" }

// 이벤트 알림 → Flutter: 인앱 웹뷰로 URL 표시
{ "type": "EVENT", "url": "https://...", "title": "이벤트 제목" }
```

---

## 📨 Kafka 이벤트 구조

### Topics & Consumer Groups

| 토픽 | Producer | Consumer | 설명 |
|------|---------|---------|------|
| `user.signInTopic` | User | Board (unimal-board-group) | 신규 회원가입 → Board에 회원 정보 동기화 |
| `user.userUpdateTopic` | User | Board (unimal-board-group) | 회원 정보 수정 (닉네임, 프로필, FCM 토큰) → Board 동기화 |
| `user.withdrawalTopic` | User | Board (unimal-board-group) | 회원 탈퇴 처리 |
| `user.reSignInTopic` | User | Board (unimal-board-group) | 재가입 시 탈퇴 상태 복구 |
| `board.likeCountCalculateTopic` | Board | Board (unimal-board-group) | 좋아요 카운트 집계 처리 |
| `board.postCountCalculateTopic` | Board | Board (unimal-board-group) | 게시글 카운트 집계 처리 |
| `board.postAppPushTopic` | Board | Notification (unimal-notification-group) | 좋아요/댓글 발생 시 FCM 푸시 발송 |

---

## 🔧 주요 기술적 특징

### 1. API Gateway 중심 인증
- Gateway가 PostgreSQL에 직접 연결해 Refresh Token 검증 및 재발급 처리
- 인증된 사용자 정보를 HTTP 헤더로 다운스트림 서비스에 전달
- 각 서비스는 별도 인증 로직 없이 비즈니스 로직에 집중

### 2. gRPC 내부 통신
`proto-common` 모듈에서 Protocol Buffers로 서비스 인터페이스를 정의합니다.

| proto 파일 | 서비스 | 설명 |
|-----------|--------|------|
| `board/board.proto` | Board | 게시판 조회 |
| `map/geocoding/reverseGeocodingProto.proto` | Map | 역지오코딩 |
| `photo/fileDeleteProto.proto` | Photo | 파일 삭제 |
| `notification/authentication/mailAuthRequestProto.proto` | Notification | 이메일 인증 |
| `notification/authentication/TelAuthRequestProto.proto` | Notification | SMS 인증 |

### 3. 공간 데이터 처리 (PostGIS)
- PostgreSQL + PostGIS 확장으로 위치 기반 공간 쿼리 처리
- JTS + Hibernate Spatial로 JPA 엔티티에서 `Point` 타입 직접 사용
- GIST 인덱스로 공간 검색 성능 최적화

### 4. ID 난독화 (Hashids)
- 게시글 ID를 외부에 노출할 때 Hashids로 인코딩
- `GET /board/hashids`, `GET /board/hashids/{value}` 로 인코딩/디코딩 가능

---

## 📊 서비스별 상세

### 🚪 API Gateway (8080)
- Spring Cloud Gateway 기반 라우팅
- JWT 인증 필터 3종 (AccessToken / RefreshToken / Optional)
- PostgreSQL 직접 연결로 Refresh Token 검증 및 재발급
- Redis 세션 관리

### 🔐 User Service (8081 / gRPC: 50081)
- 회원가입, 로그인, 프로필 관리 REST API
- OAuth2 소셜 로그인 (Kakao, Naver, Google)
- 이메일/SMS 인증 (gRPC로 Notification Service 호출)
- FCM 디바이스 토큰 관리 (`MemberDevice`)
- Kafka Producer: `user.*` 토픽 발행

### 🗺️ Map Service (8082 / gRPC: 50082)
- Google Geocoding API 연동 역지오코딩
- gRPC Server: `ReverseGeocodingService` 제공

### 📝 Board Service (8083 / gRPC: 50083)
- 게시글/댓글/좋아요/공지사항 CRUD
- PostGIS 기반 위치 게시물 조회 (줌 레벨별 반경 + 복합 점수 정렬)
- Kafka Consumer: `user.*` 토픽 → `BoardMember` 동기화
- Kafka Producer: `board.*` 토픽 발행

### 📸 Photo Service (8084 / gRPC: 50084)
- AWS S3 파일 업로드/삭제 (AWS SDK 2.32.26)
- AWS CRT Client 기반 고성능 Transfer Manager
- CloudFront CDN URL 반환 (`cdn.unimal.co.kr`)
- gRPC Server: `FileDeleteService` 제공

### 🔔 Notification Service (8085 / gRPC: 50085)
- Firebase Admin SDK로 FCM 단건/멀티캐스트 발송
- Gmail SMTP + Thymeleaf 이메일 템플릿
- Naver Cloud SMS 문자 인증
- Redis 인증 코드 임시 저장 (TTL 관리)
- Kafka Consumer: `board.postAppPushTopic` → FCM 발송

---

## 🗄️ 데이터베이스 스키마

각 서비스는 독립된 PostgreSQL 스키마를 사용합니다.

| 스키마 | 서비스 | 주요 테이블 |
|--------|--------|-----------|
| `unimal_user` | User | member, member_device, authentication_token |
| `unimal_board` | Board | board, board_file, board_like, board_reply, board_member, notice |
| `unimal_map` | Map | — |
| `unimal_photo` | Photo | — |
| `unimal_notification` | Notification | — |

---

## 🚀 CI/CD 파이프라인

GitHub Actions (`/.github/workflows/ci-cd.yml`) 를 통해 `master` 브랜치 푸시 시 **변경된 모듈만** 빌드·배포합니다.

```
Push to master
  ↓
detect-changes (dorny/paths-filter)
  ↓ 변경된 모듈만 선택
build & push Docker image (ECR)
  ↓
deploy to server (SSH + docker-compose pull & up)
```

**모듈별 변경 감지 경로:**
- `api-gateway/**`, `common/**`, `build.gradle.kts` → api-gateway 빌드
- `user/**`, `proto-common/**`, `web-common/**` → user 빌드
- `board/**` → board 빌드
- `photo/**` → photo 빌드
- `notification/**` → notification 빌드
- `docker-compose*.yml` → 설정 동기화만 수행

---

## 📱 Flutter 앱 연동 포인트

| 기능 | Flutter 서비스 | 백엔드 엔드포인트 |
|------|--------------|----------------|
| 소셜 로그인 | `KakaoLoginService`, `NaverLoginService`, `GoogleLoginService` | `GET/POST /user/auth/login/mobile/*` |
| 토큰 재발급 | `ApiClient` (401 자동 재발급) | `GET /user/auth/token-reissue` |
| 지도 마커 | `MapPostService` | `GET /board/map/location/post?latitude=&longitude=&zoom=` |
| 게시글 작성 | `BoardApiService.createBoard()` | `POST /board/post` (Multipart) |
| FCM 토큰 등록 | `PushNotificationService.initialize()` | `POST /user/member/device/info/update` |
| 역지오코딩 | `GeocodingApiService` | `GET /map/reverse-geocoding` |
| 프로필 이미지 | `UserInfoService.uploadProfileImage()` | `POST /user/member/profile/image/upload` |

---

## ⚙️ 로컬 환경 설정

### 요구사항
- Java 21
- Docker & Docker Compose

### 환경 변수 설정 (`.env`)

```env
# 서비스 포트
API_GATEWAY_SERVICE_PORT=8080
USER_SERVICE_PORT=8081
MAP_SERVICE_PORT=8082
BOARD_SERVICE_PORT=8083
PHOTO_SERVICE_PORT=8084
NOTIFICATION_SERVICE_PORT=8085

# 데이터베이스
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=unimal
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
DATABASE_USER_SCHEMA=unimal_user
DATABASE_BOARD_SCHEMA=unimal_board

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password

# JWT (Base64 Encoded HS256 Secret)
JWT_SECRET_KEY=your_base64_encoded_secret_key

# Kafka
KAFKA_SERVER_1=localhost:9091
KAFKA_SERVER_2=localhost:9092
KAFKA_SERVER_3=localhost:9093

# OAuth
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# Google
GOOGLE_GEOCODING_API_KEY=your_google_api_key

# AWS
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_S3_BUCKET_NAME=unimal-bucket
AWS_REGION=ap-northeast-2

# Email
SMTP_MAIL_USERNAME=support@unimal.co.kr
SMTP_MAIL_PASSWORD=your_smtp_password

# Naver Cloud SMS
NAVER_CLOUD_SMS_SERVICE_ID=your_service_id
NAVER_CLOUD_SMS_ACCESS_KEY=your_access_key
NAVER_CLOUD_SMS_SECRET_KEY=your_secret_key
```

### Docker Compose 구성

| 파일 | 설명 |
|------|------|
| `docker-compose.yml` | 전체 마이크로서비스 |
| `docker-compose-db.yml` | PostgreSQL + PostGIS |
| `docker-compose-kafka.yml` | Kafka 3 브로커 클러스터 |
| `docker-compose-monitoring.yml` | Prometheus + Grafana |
| `docker-compose-local.yml` | 로컬 개발 통합 환경 |

### 시작하기

```bash
# 1. 리포지토리 클론
git clone https://github.com/daeyoung-k/unimal-server.git
cd unimal-server

# 2. .env 파일 생성 (위 환경 변수 참고)

# 3. 인프라 실행 (DB, Redis, Kafka)
docker-compose -f docker-compose-db.yml -f docker-compose-kafka.yml up -d

# 4. 프로젝트 빌드
./gradlew clean build

# 5. 전체 서비스 실행
docker-compose up -d

# 6. 헬스 체크
curl http://localhost:8080/actuator/health
```

---

## 📈 모니터링

- **Prometheus**: 각 서비스 `/actuator/prometheus` 메트릭 수집
- **Grafana**: 대시보드 시각화
- **Spring Actuator**: `/actuator/health`, `/actuator/metrics`

---

<div align="center">

**🗺️ 스토맵 (Stomap) — 지금 여기, 나의 이야기**

Made with ❤️ by Kane

</div>
