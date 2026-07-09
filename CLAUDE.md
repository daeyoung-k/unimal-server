# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**스토맵(Stomap)** 백엔드 — Kotlin + Spring Boot 3 기반 멀티모듈 마이크로서비스. Flutter 앱이 `api.unimal.co.kr`(포트 8080 API Gateway)를 단일 진입점으로 호출한다.

## Commands

```bash
# 전체 빌드
./gradlew clean build

# 특정 모듈만 빌드
./gradlew :board:build
./gradlew :user:build

# 테스트 실행
./gradlew test
./gradlew :board:test

# 인프라 실행 (DB + Redis + Kafka)
docker-compose -f docker-compose-db.yml -f docker-compose-kafka.yml up -d

# 전체 서비스 실행
docker-compose up -d

# 로컬 통합 환경 (인프라 + 서비스 전체)
docker-compose -f docker-compose-local.yml up -d

# 헬스 체크
curl http://localhost:8080/actuator/health

# proto 코드 생성 (변경 시)
./gradlew :proto-common:generateProto
```

## Architecture

### 모듈 구조

```
unimal-server/
├── api-gateway/      # 포트 8080 — 단일 진입점, JWT 인증 필터, 라우팅
├── user/             # 포트 8081 / gRPC 50081 — 인증, 회원, OAuth2
├── map/              # 포트 8082 / gRPC 50082 — 역지오코딩
├── board/            # 포트 8083 / gRPC 50083 — 게시글, 댓글, 좋아요, 지도 마커
├── photo/            # 포트 8084 / gRPC 50084 — AWS S3 파일 업로드/삭제
├── notification/     # 포트 8085 / gRPC 50085 — FCM 푸시, 이메일, SMS 인증
├── common/           # 공유 라이브러리 — QueryDSL, Hashids, CommonResponse, Kafka DTO
├── web-common/       # 웹 공통 — JWT 유틸, @UserInfo 어노테이션
├── proto-common/     # gRPC Protocol Buffers 정의 (.proto 파일)
└── admin/            # 어드민 모듈
```

### 통신 방식

| 방향 | 방식 |
|------|------|
| Flutter → Backend | REST (API Gateway 8080 경유) |
| Service ↔ Service | gRPC (proto-common 정의) |
| 이벤트 | Apache Kafka (3 브로커 클러스터) |

### API Gateway 인증 필터

Gateway가 JWT 검증 후 사용자 정보를 HTTP 헤더로 다운스트림 서비스에 전달한다. **각 서비스는 별도 인증 로직 없이** 이 헤더만 읽으면 된다.

| 헤더 | 내용 |
|------|------|
| `X-Unimal-Email` | 사용자 이메일 |
| `X-Unimal-Access-Token` | Access Token |
| `X-Unimal-Refresh-Token` | Refresh Token |
| `X-Unimal-Provider` | OAuth 제공자 (kakao/naver/google) |

필터 3종:
- `AccessTokenFilter` — 인증 필수 API
- `RefreshTokenFilter` — 토큰 재발급·로그아웃·탈퇴
- `OptionalAccessTokenFilter` — 인증 선택 (게시글 조회 등)

### 각 서비스 내부 패키지 구조 (board 기준)

```
board/src/main/kotlin/com/unimal/board/
├── controller/          # REST 컨트롤러 + DTO
│   ├── post/            # PostController (게시글/댓글/좋아요)
│   ├── map/             # MapController (지도 마커 조회)
│   ├── notice/          # NoticeController
│   └── report/          # ReportController
├── service/             # 비즈니스 로직
│   └── post/manager/    # LikeManager, PostManager, ReplyManager (세분화)
├── domain/              # JPA 엔티티 + Repository
├── kafka/               # Consumer / Producer / Topic 정의
├── grpc/                # gRPC 서버 구현
├── config/              # Spring 설정 (QueryDSL, Geometry, Redis, CORS)
├── enums/               # 열거형
└── utils/               # HashidsUtil, RedisCacheManager
```

### 지도 마커 조회 핵심 로직 (`board` 서비스)

`GET /board/map/location/post?latitude=&longitude=&zoom=`

- PostGIS `ST_DWithin` + GIST 인덱스 → 줌 레벨별 반경(100m~50km) 내 공간 필터링
- `INNER JOIN LATERAL board_file` → 대표 이미지 1장, **사진 없는 게시물 제외**
- score 계산 및 정렬을 DB 레벨에서 처리:
  ```
  score = 본인글 보너스(+10,000) + 신선도(+0.1~1.0) + 좋아요×2 + 댓글×3
  ```
- Flutter에서 `globalZIndex = 200000 + score`로 마커 우선순위 결정

### Kafka 이벤트 흐름

| 토픽 | Producer → Consumer | 목적 |
|------|---------------------|------|
| `user.signInTopic` | User → Board | 신규 가입 시 BoardMember 동기화 |
| `user.userUpdateTopic` | User → Board | 닉네임/프로필/FCM 토큰 변경 동기화 |
| `user.withdrawalTopic` | User → Board | 회원 탈퇴 처리 |
| `user.reSignInTopic` | User → Board | 재가입 시 탈퇴 상태 복구 |
| `board.likeCountCalculateTopic` | Board → Board | 좋아요 카운트 집계 |
| `board.postCountCalculateTopic` | Board → Board | 게시글 카운트 집계 |
| `board.postAppPushTopic` | Board → Notification | 좋아요/댓글 → FCM 발송 |

### gRPC 서비스 (proto-common)

| proto 파일 | 제공 서비스 | 호출자 |
|-----------|-----------|--------|
| `board/board.proto` | Board | — |
| `map/geocoding/reverseGeocodingProto.proto` | Map | Board |
| `photo/fileDeleteProto.proto` | Photo | Board |
| `notification/authentication/mailAuthRequestProto.proto` | Notification | User |
| `notification/authentication/TelAuthRequestProto.proto` | Notification | User |

### 데이터베이스 스키마

각 서비스는 독립된 PostgreSQL 스키마 사용:

| 스키마 | 서비스 | 주요 테이블 |
|--------|--------|-----------|
| `unimal_user` | User | member, member_device, authentication_token |
| `unimal_board` | Board | board, board_file, board_like, board_reply, board_member, notice |

### 외부 서비스 연동

| 서비스 | 모듈 | 용도 |
|--------|------|------|
| AWS S3 + CloudFront | photo | 파일 저장, CDN URL (`cdn.unimal.co.kr`) |
| Firebase Admin SDK | notification | FCM 단건/멀티캐스트 푸시 |
| Google Geocoding API | map | 역지오코딩 (위도·경도 → 주소) |
| Naver Cloud SMS | notification | SMS 인증 코드 |
| Gmail SMTP + Thymeleaf | notification | 이메일 인증 |
| Redis | user, board, notification | 인증 코드 TTL 저장, 세션 관리 |

## 환경 설정

루트에 `.env` 파일 필요. 주요 항목:

```env
# 서비스 포트
API_GATEWAY_SERVICE_PORT=8080
USER_SERVICE_PORT=8081
MAP_SERVICE_PORT=8082
BOARD_SERVICE_PORT=8083
PHOTO_SERVICE_PORT=8084
NOTIFICATION_SERVICE_PORT=8085

# DB
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=unimal
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=...
DATABASE_USER_SCHEMA=unimal_user
DATABASE_BOARD_SCHEMA=unimal_board

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=...

# JWT
JWT_SECRET_KEY=...   # Base64 인코딩된 HS256 시크릿

# Kafka
KAFKA_SERVER_1=localhost:9091
KAFKA_SERVER_2=localhost:9092
KAFKA_SERVER_3=localhost:9093

# OAuth
KAKAO_CLIENT_ID=...
KAKAO_CLIENT_SECRET=...
GOOGLE_GEOCODING_API_KEY=...

# AWS
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_S3_BUCKET_NAME=unimal-bucket
AWS_REGION=ap-northeast-2

# Email/SMS
SMTP_MAIL_USERNAME=support@unimal.co.kr
SMTP_MAIL_PASSWORD=...
NAVER_CLOUD_SMS_SERVICE_ID=...
NAVER_CLOUD_SMS_ACCESS_KEY=...
NAVER_CLOUD_SMS_SECRET_KEY=...
```

## CI/CD

GitHub Actions — `master` 브랜치 푸시 시 **변경된 모듈만** 빌드·배포:

- `api-gateway/**` or `common/**` or `build.gradle.kts` → api-gateway 빌드
- `user/**` or `proto-common/**` or `web-common/**` → user 빌드
- `board/**` → board 빌드
- `photo/**` → photo 빌드
- `notification/**` → notification 빌드
- `docker-compose*.yml` → 설정 동기화만 수행 (재빌드 없음)

## Flutter 앱 연동 포인트

| 기능 | Flutter 클래스 | 백엔드 엔드포인트 |
|------|--------------|----------------|
| 소셜 로그인 | `KakaoLoginService` 등 | `GET/POST /user/auth/login/mobile/*` |
| 토큰 재발급 | `ApiClient` (401 자동) | `GET /user/auth/token-reissue` |
| 지도 마커 | `MapPostService` | `GET /board/map/location/post` |
| 게시글 작성 | `BoardApiService.createBoard()` | `POST /board/post` (Multipart) |
| FCM 토큰 등록 | `PushNotificationService` | `POST /user/member/device/info/update` |
| 역지오코딩 | `GeocodingApiService` | `GET /map/reverse-geocoding` |
| 프로필 이미지 | `UserInfoService` | `POST /user/member/profile/image/upload` |

로그인 성공 시 응답 헤더(`X-Unimal-Access-Token`, `X-Unimal-Refresh-Token`, `X-Unimal-Email`, `X-Unimal-Provider`)를 Flutter `AccountService`가 파싱해 `SecureStorage`에 저장한다.

## 📄 문서 작성 규칙 (docs/)

설계·계획·할일·트러블슈팅 문서는 **반드시 루트 `docs/` 아래에만** 만든다. 모듈 폴더(`board/`, `user/` 등) 안에 `.md` 설계 문서를 만들지 않는다.

| 폴더 | 용도 | 명명 |
| --- | --- | --- |
| `docs/architecture/` | 시스템 구조·설계 결정(ADR)·방향성 (살아있는 문서) | 주제 기반 (`system-overview.md`) |
| `docs/specs/` | 기능별 설계 — 무엇을/왜 | `YYYY-MM-DD-주제.md` |
| `docs/plans/` | 구현 계획 — 어떻게/단계 | `YYYY-MM-DD-주제.md` |
| `docs/todo/` | 할일·백로그 | 주제 기반 |
| `docs/troubleshooting/` | 디버깅·장애 기록 | `YYYY-MM-DD-주제.md` |

- 자동화 도구(superpowers 등)가 `docs/superpowers/...`에 문서를 생성하면, 작업 종료 전 위 카테고리 폴더로 옮기고 `superpowers/`는 비운다.
- 한글 파일명은 공백 대신 하이픈(`-`)을 쓴다.
- 자세한 규칙은 `docs/README.md` 참고.

## Git 커밋 규칙

- **중간 커밋 금지.** 작업(요청받은 과제) 단위로 나눠서 커밋하지 않는다.
- 요청받은 작업이 **전부 끝난 뒤 한 번에 커밋**한다 (빌드/테스트 통과 확인 후).
- 사용자가 명시적으로 요청하기 전에는 커밋하지 않는다.
