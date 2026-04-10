# 🐾 Unimal - Multi-Module Microservices Backend

> **우리 주변 야생 & 반려동물을 함께 공유하는 커뮤니티 및 위치 기반 서비스를 제공하는 멀티모듈 마이크로서비스 백엔드 시스템**

![Kotlin](https://img.shields.io/badge/Kotlin_1.9.25-7F52FF?style=flat&logo=Kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.3-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC_1.72-4285F4?style=flat&logo=grpc&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_+_PostGIS-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white)

## 📋 프로젝트 개요

**사용자들이 위치 기반으로 야생동물과 반려동물의 사진과 정보를 지도에 공유**할 수 있는 커뮤니티 기반 플랫폼입니다.
마이크로서비스 아키텍처를 기반으로 한 확장 가능하고 유지보수가 용이한 백엔드 시스템입니다.

### 🎯 핵심 기능
- **사용자 인증/인가** - JWT + OAuth2 (Kakao, Naver, Google) 기반 보안 시스템
- **게시판 시스템** - 동물 커뮤니티 게시글, 댓글, 좋아요
- **위치 기반 게시물** - PostGIS 공간 필터링 + 복합 점수 정렬 마커 제공
- **사진 관리** - AWS S3 + CloudFront CDN 연동
- **푸시 알림** - Firebase Admin SDK (FCM) 기반 앱 푸시
- **이메일/SMS 인증** - Gmail SMTP + Naver Cloud SMS

---

## 🏗️ 시스템 아키텍처

### 마이크로서비스 구조

```
unimal-server/
├── api-gateway/       # API 게이트웨이 (포트: 8080)
├── user/              # 사용자 관리 서비스 (포트: 8081, gRPC: 50081)
├── map/               # 위치/역지오코딩 서비스 (포트: 8082, gRPC: 50082)
├── board/             # 게시판 서비스 (포트: 8083, gRPC: 50083)
├── photo/             # 사진 관리 서비스 (포트: 8084, gRPC: 50084)
├── notification/      # 알림 서비스 (포트: 8085, gRPC: 50085)
├── common/            # 공통 유틸리티 (QueryDSL, Hashids, CommonResponse)
├── web-common/        # 웹 공통 컴포넌트 (JWT 유틸)
└── proto-common/      # gRPC Protocol Buffers 정의
```

### 통신 아키텍처

| 통신 방향 | 방식 | 설명 |
|---------|------|------|
| Client → Backend | REST / HTTP | API Gateway를 단일 진입점으로 사용 |
| Service ↔ Service | gRPC | 높은 성능, 타입 안전 내부 통신 |
| 이벤트 기반 | Apache Kafka | 비동기 이벤트 처리 (알림, 파일 삭제 등) |

**Header 기반 사용자 정보 전파** (API Gateway → 각 서비스):
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
| 빌드 | Gradle Kotlin DSL | - |
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
| 공간 라이브러리 | JTS + Hibernate Spatial | 1.20.0 / 6.6.8.Final |
| 캐시/세션 | Redis | - |

### 보안 & 인증
| 분류 | 기술 | 비고 |
|------|------|------|
| 토큰 | JWT (JJWT) | 0.12.6, HS256 |
| 소셜 로그인 | OAuth2 | Kakao, Naver, Google |
| 전화 인증 | Naver Cloud SMS | - |
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
| 모니터링 | Prometheus + Grafana |
| 헬스 체크 | Spring Boot Actuator |

---

## 📡 API 엔드포인트

### 🔐 Auth (`/auth`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/auth/login/mobile/kakao` | Kakao 소셜 로그인 |
| POST | `/auth/login/mobile/naver` | Naver 소셜 로그인 |
| POST | `/auth/login/mobile/google` | Google 소셜 로그인 |
| POST | `/auth/login/manual` | 수동 로그인 |
| POST | `/auth/signup/manual` | 회원가입 |
| GET | `/auth/token-reissue` | Access Token 재발급 |
| GET | `/auth/logout` | 로그아웃 |
| GET | `/auth/withdrawal` | 회원탈퇴 |
| POST | `/auth/email/code-request` | 이메일 인증 코드 요청 |
| POST | `/auth/email/code-verify` | 이메일 인증 코드 검증 |
| POST | `/auth/tel/code-request` | 전화번호 인증 코드 요청 |
| POST | `/auth/tel/code-verify` | 전화번호 인증 코드 검증 |
| POST | `/auth/email-tel/code-request` | 이메일+전화번호 통합 인증 요청 |
| POST | `/auth/email-tel/code-verify` | 이메일+전화번호 통합 인증 검증 |
| POST | `/auth/tel/check-update` | 전화번호 업데이트 |

### 👤 Member (`/member`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/member/info` | 사용자 정보 조회 |
| PATCH | `/member/info/update` | 사용자 정보 수정 |
| POST | `/member/change/password` | 비밀번호 변경 |
| POST | `/member/find/email` | 이메일 찾기 |
| POST | `/member/find/email-tel/check/request` | 이메일+전화번호 확인 |
| POST | `/member/find/change/password` | 비밀번호 재설정 |
| GET | `/member/find/nickname/duplicate` | 닉네임 중복 확인 |
| POST | `/member/find/email/duplicate` | 이메일 중복 확인 |
| POST | `/member/find/tel/duplicate` | 전화번호 중복 확인 |
| POST | `/member/device/info/update` | 디바이스 정보 업데이트 (FCM 토큰) |
| POST | `/member/profile/image/upload` | 프로필 이미지 업로드 |

### 📝 Board Post (`/board/post`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/board/post/{boardId}` | 게시글 조회 |
| GET | `/board/post/list` | 게시글 목록 (페이징) |
| GET | `/board/post/my/list` | 내 게시글 목록 |
| POST | `/board/post` | 게시글 작성 (멀티파트) |
| PATCH | `/board/post/{boardId}/update` | 게시글 수정 |
| POST | `/board/post/{boardId}/file/upload` | 게시글 파일 업로드 |
| POST | `/board/post/{boardId}/file/delete` | 게시글 파일 삭제 |
| DELETE | `/board/post/{boardId}/delete` | 게시글 삭제 |
| GET | `/board/post/{boardId}/like` | 좋아요 토글 |
| POST | `/board/post/{boardId}/reply` | 댓글 작성 |
| GET | `/board/post/{boardId}/reply` | 댓글 목록 |
| PATCH | `/board/post/{boardId}/reply/{replyId}/update` | 댓글 수정 |
| DELETE | `/board/post/{boardId}/reply/{replyId}/delete` | 댓글 삭제 |
| GET | `/board/post/total/like` | 총 좋아요 수 |
| GET | `/board/post/total` | 총 게시글 수 |

### 🗺️ Board Map (`/board/map`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/board/map/location/post` | 위치 기반 게시물 조회 |

### 📢 Notice (`/board/notice`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/board/notice/{id}` | 공지사항 조회 |
| GET | `/board/notice` | 공지사항 목록 |
| POST | `/board/notice` | 공지사항 작성 |

### 🔢 Hashids (`/board`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/board/hashids` | ID 인코딩 |
| GET | `/board/hashids/{value}` | ID 디코딩 |

### 📍 Map (`/map`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/map/reverse-geocoding` | 역지오코딩 (위도/경도 → 주소) |

### 📸 Photo (`/photo`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/photo/upload` | 단일 파일 업로드 (S3) |
| POST | `/photo/multiple-upload` | 다중 파일 업로드 (S3) |
| POST | `/photo/delete` | 파일 삭제 (S3) |

### 🔔 Notification (`/notification`)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/notification/app-push` | 단일 사용자 FCM 푸시 발송 |
| POST | `/notification/app-push/multicast` | 다중 사용자 FCM 푸시 발송 |

---

## 🗺️ 지도 기반 게시물 조회 상세

`GET /board/map/location/post`

사진이 등록된 게시물만 반환하며, 복합 점수로 정렬합니다.

### 요청 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `latitude` | Double | 지도 중심 위도 |
| `longitude` | Double | 지도 중심 경도 |
| `zoom` | Int | 줌 레벨 (10 ~ 20) |

### 줌 레벨별 조회 범위
| 줌 | 조회 반경 | 최대 게시물 수 |
|----|---------|-------------|
| 10 | 50km | 30 |
| 11 | 30km | 30 |
| 12 | 20km | 30 |
| 13 | 10km | 40 |
| 14 | 5km | 40 |
| 15 | 3km | 50 |
| 16 | 2km | 50 |
| 17 | 1km | 50 |
| 18 | 500m | 100 |
| 19 | 300m | 100 |
| 20 | 100m | 100 |

### 응답 필드
| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | String | 게시물 ID (Hashids 인코딩) |
| `title` | String? | 제목 |
| `content` | String | 내용 |
| `street_name` | String? | 도로명 주소 |
| `latitude` | Double | 게시물 위도 |
| `longitude` | Double | 게시물 경도 |
| `created_at` | LocalDateTime | 작성 시각 |
| `file_url` | String? | 대표 이미지 URL (CloudFront) |
| `like_count` | Long | 좋아요 수 |
| `reply_count` | Long | 댓글 수 |
| `score` | Double | 복합 점수 (0.0 ~ 20000.0, 마커 우선순위) |

### 점수 계산 알고리즘
```
score = 본인 게시물 보너스 + 신선도 점수 + 참여도 점수

본인 게시물 보너스 : 내 글이면 +10000
신선도 점수       : 30분 이내 1.0 / 2시간 이내 0.6 / 6시간 이내 0.3 / 이후 0.1
참여도 점수       : 좋아요 × 2 + 댓글 × 3
```

### 구현 상세
- PostGIS `ST_DWithin` + GIST 인덱스로 공간 필터링 O(log n)
- `INNER JOIN LATERAL board_file`로 대표 이미지 1장만 조회, 사진 없는 게시물 제외
- `ST_Y` / `ST_X`로 위도·경도 추출
- 점수 계산 및 정렬을 DB 내에서 처리 (앱단 반복문 없음)

---

## 🔔 앱 푸시 알림 (FCM)

Flutter 앱에서 수신하는 푸시 알림의 `data` 페이로드 스펙입니다.

### 알림 타입별 data 필드

#### 좋아요 / 댓글 알림
```json
{
  "type": "LIKE",
  "target_id": "{게시글 ID}"
}
```
```json
{
  "type": "REPLY",
  "target_id": "{게시글 ID}"
}
```
→ 앱: 해당 게시글 상세 화면으로 이동

#### 공지사항 알림
```json
{
  "type": "NOTICE"
}
```
→ 앱: 공지사항 목록 화면으로 이동

#### 이벤트 알림
```json
{
  "type": "EVENT",
  "url": "https://example.com/event",
  "title": "이벤트 제목"
}
```
→ 앱: 인앱 웹뷰로 해당 URL 표시

---

## 🔧 주요 기술적 특징

### 1. API 게이트웨이 중심의 인증
- **Refresh Token 관리**: 게이트웨이가 PostgreSQL에 직접 연결하여 Refresh Token 유효성 검증 및 재발급 처리
- **Header 기반 정보 전파**: 인증된 사용자 정보를 HTTP 헤더로 다운스트림 서비스에 전달, 각 서비스는 별도 인증 없이 비즈니스 로직에 집중

### 2. gRPC 내부 통신
`proto-common` 모듈에서 Protocol Buffers로 서비스 인터페이스를 정의하고 타입 안전한 코드를 자동 생성합니다.

| proto 파일 | 서비스 | 설명 |
|-----------|--------|------|
| `board/board.proto` | Board Service | 게시판 조회 |
| `map/geocoding/reverseGeocodingProto.proto` | Map Service | 역지오코딩 |
| `photo/fileDeleteProto.proto` | Photo Service | 파일 삭제 |
| `notification/authentication/mailAuthRequestProto.proto` | Notification Service | 이메일 인증 |
| `notification/authentication/TelAuthRequestProto.proto` | Notification Service | SMS 인증 |

### 3. 이벤트 드리븐 아키텍처 (Kafka)
- **Producer**: User, Board, Photo 서비스에서 도메인 이벤트 발행
- **Consumer Groups**: `unimal-user-group`, `unimal-board-group`, `unimal-photo-group`, `unimal-notification-group`
- 회원가입 → 환영 이메일, 게시글 작성 → 알림 발송 등을 비동기 처리

### 4. 공간 데이터 처리 (PostGIS)
- PostgreSQL + PostGIS 확장으로 위치 기반 공간 쿼리 처리
- JTS + Hibernate Spatial로 JPA 엔티티에서 Point 타입 직접 사용
- GIST 인덱스로 공간 검색 성능 최적화

---

## 📊 서비스별 상세

### 🚪 API Gateway (8080)
- Spring Cloud Gateway 기반 라우팅
- JWT 인증 필터 (Access Token 검증)
- PostgreSQL 직접 연결로 Refresh Token 검증 및 재발급
- Redis 세션 관리

### 🔐 User Service (8081 / gRPC 50081)
- 회원가입, 로그인, 프로필 관리 REST API
- OAuth2 소셜 로그인 (Kakao, Naver, Google)
- 이메일/SMS 인증 (gRPC로 Notification Service 호출)
- FCM 디바이스 토큰 관리
- Kafka Producer: 회원 관련 이벤트 발행

### 🗺️ Map Service (8082 / gRPC 50082)
- Google Geocoding API 연동 역지오코딩
- gRPC Server: `ReverseGeocodingService` 제공 (User Service에서 호출)

### 📝 Board Service (8083 / gRPC 50083)
- 게시글/댓글/좋아요 CRUD
- PostGIS 기반 위치 게시물 조회 (복합 점수 정렬)
- 공지사항 관리
- gRPC Server: `BoardService` 제공
- Kafka Producer: 게시글 이벤트 발행

### 📸 Photo Service (8084 / gRPC 50084)
- AWS S3 파일 업로드/삭제 (AWS SDK 2.32.26)
- AWS CRT Client 기반 고성능 Transfer Manager
- CloudFront CDN URL 반환 (cdn.unimal.co.kr)
- gRPC Server: `FileDeleteService` 제공
- Kafka Consumer: 파일 삭제 이벤트 처리

### 🔔 Notification Service (8085 / gRPC 50085)
- Firebase Admin SDK로 FCM 앱 푸시 발송 (단일/멀티캐스트)
- Gmail SMTP + Thymeleaf 이메일 템플릿
- Naver Cloud SMS 문자 인증
- Redis 인증 코드 임시 저장
- gRPC Server: `MailAuthRequestService`, `TelAuthRequestService` 제공
- Kafka Consumer: 알림 이벤트 처리

---

## 🗄️ 데이터베이스 스키마

각 서비스는 독립된 PostgreSQL 스키마를 사용합니다.

| 스키마 | 서비스 | 주요 테이블 |
|--------|--------|-----------|
| `unimal_user` | User | member, member_device, member_role, authentication_token |
| `unimal_board` | Board | board, board_file, board_like, board_reply, notice |
| `unimal_map` | Map | - |
| `unimal_photo` | Photo | - |
| `unimal_notification` | Notification | - |

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

---

## 🚀 프로젝트 시작하기

```bash
# 1. 리포지토리 클론
git clone https://github.com/your-username/unimal-server.git
cd unimal-server

# 2. .env 파일 생성 (위 환경 변수 참고)

# 3. 인프라 실행 (DB, Redis, Kafka)
docker-compose -f docker-compose-db.yml -f docker-compose-kafka.yml up -d

# 4. 프로젝트 빌드
./gradlew clean build

# 5. 전체 서비스 실행
docker-compose up -d

# 6. 서비스 확인
docker-compose ps
# API Gateway: http://localhost:8080
# Actuator:    http://localhost:8080/actuator/health
```

---

## 📈 모니터링

- **Prometheus**: 각 서비스 `/actuator/prometheus` 메트릭 수집
- **Grafana**: 대시보드 시각화
- **Spring Actuator**: `/actuator/health`, `/actuator/metrics`

---

<div align="center">

**🐾 Unimal - 우리 주변 동물 🐾**

Made with ❤️ by Kane

</div>
