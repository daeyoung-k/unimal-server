# 🐾 Unimal - Multi-Module Microservices Backend

> **우리 주변 야생 & 반려동물을 함께 공유하는 커뮤니티 및 위치 기반 서비스를 제공하는 멀티모듈 마이크로서비스 백엔드 시스템**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=Kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-4285F4?style=flat&logo=grpc&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white)

## 📋 프로젝트 개요

**사용자들이 위치 기반으로 야생동물과 반려동물의 사진과 정보를 지도에 공유**할 수 있는 커뮤니티 기반 플랫폼입니다.
AI를 활용해 **업로드된 동물 사진을 자동으로 식별하고 캐릭터처럼 애니화하는 기능**도 제공하여 사용자 참여를 유도하고 즐거움을 더합니다.
마이크로서비스 아키텍처를 기반으로 한 확장 가능하고 유지보수가 용이한 백엔드 시스템을 구축했습니다.

### 🎯 핵심 기능
- **사용자 인증/인가** - JWT 기반 보안 시스템
- **게시판 시스템** - 반려동물 관련 커뮤니티
- **사진 관리** - AWS S3 연동 이미지 업로드/관리
- **위치 기반 서비스** - 주변 펫샵, 병원 등 정보 제공
- **실시간 알림** - Kafka 기반 이벤트 드리븐 알림 시스템

## 🏗️ 시스템 아키텍처

### 마이크로서비스 구조
```
unimal-server/
├── api-gateway/          # API 게이트웨이 (8080)
├── user/                # 사용자 관리 서비스 (8081, gRPC: 50081)
├── map/                 # 위치 기반 서비스 (8082, gRPC: 50082)
├── board/               # 게시판 서비스 (8083, gRPC: 50083)
├── photo/               # 사진 관리 서비스 (8084, gRPC: 50084)
├── notification/        # 알림 서비스 (8085, gRPC: 50085)
├── common/              # 공통 유틸리티
├── web-common/          # 웹 공통 컴포넌트
└── proto-common/        # gRPC Protocol Buffers 정의
```

### 통신 아키텍처
- **외부 통신 (Client → Backend)**: 모든 클라이언트 요청은 **API Gateway**를 통해 들어옵니다. Spring Cloud Gateway로 구현된 게이트웨이는 JWT 기반의 인증을 처리하고, 각 요청을 적절한 마이크로서비스로 라우팅합니다. 인증된 사용자의 정보는 `X-Unimal-User-*` 형태의 HTTP 헤더에 담겨 내부 서비스로 전파됩니다.
- **내부 통신 (Inter-Service)**: 서비스 간 통신은 주로 **gRPC**를 사용하여 높은 성능과 타입 안정성을 보장합니다. 예를 들어, `User` 서비스는 `Board`나 `Notification` 서비스의 기능을 gRPC 클라이언트로 호출합니다.
- **비동기 통신**: 서비스 간의 의존성을 낮추고 이벤트 기반 아키텍처를 구현하기 위해 **Apache Kafka**를 사용합니다. 예를 들어, 회원가입 시 환영 이메일을 보내는 작업은 Kafka 이벤트를 통해 비동기적으로 처리됩니다.

### 기술 스택

**Backend Framework**
- **Kotlin** + **Spring Boot** + **JPA**
- **Java 21** + **Gradle Kotlin DSL**

**Communication**
- **Spring Cloud Gateway** - 외부 요청 라우팅 및 인증
- **gRPC** - 내부 서비스 간 동기 통신 (MSA to MSA)
- **Apache Kafka** - 이벤트 기반 비동기 통신

**Database & Cache**
- **PostgreSQL** - 주 데이터베이스
- **Redis** - 캐싱 및 세션 관리

**Infrastructure**
- **Docker** + **Docker Compose** - 컨테이너화 및 로컬 개발 환경
- **AWS S3** - 파일 저장소 (향후 연동 예정)

## 🔧 주요 기술적 특징

### 1. API 게이트웨이 중심의 인증
**API Gateway**는 단순한 프록시를 넘어, 인증의 핵심적인 역할을 수행합니다.
- **Refresh Token 관리**: 일반적인 MSA 구조와 달리, 게이트웨이는 **PostgreSQL 데이터베이스에 직접 연결**하여 Refresh Token의 유효성을 검증합니다. 이는 토큰 재발급 로직을 게이트웨이에서 처리하여 내부 인증 서버의 부담을 줄이고 전체적인 구조를 단순화합니다.
- **Header 기반 정보 전파**: 인증된 요청에 대해, 게이트웨이는 사용자의 이메일, 닉네임, 역할 등의 정보를 HTTP 헤더에 추가하여 다운스트림 서비스로 전달합니다. 이를 통해 각 마이크로서비스는 별도의 인증 과정 없이 사용자 정보를 신뢰하고 비즈니스 로직에 집중할 수 있습니다.

### 2. gRPC와 Protocol Buffers
- **gRPC**: 내부 서비스 간의 통신은 HTTP/JSON 대비 높은 성능과 낮은 지연 시간을 보이는 gRPC를 사용합니다. `proto-common` 모듈에서 Protocol Buffers를 사용하여 서비스 인터페이스와 데이터 모델을 명확하게 정의하고, 이를 바탕으로 타입-세이프한 클라이언트와 서버 코드를 생성합니다.
- **Service Discovery**: Docker Compose 환경에서는 서비스 이름을 호스트명으로 사용하여 gRPC 클라이언트가 각 서버를 찾습니다.

### 3. 이벤트 드리븐 아키텍처
- **Apache Kafka**: 회원가입, 게시글 작성 등 주요 도메인 이벤트가 발생하면, 관련 서비스는 Kafka 토픽으로 이벤트를 발행합니다. `Notification` 서비스와 같은 다른 서비스들은 이 이벤트를 구독하여 실시간 알림 발송 등의 후속 조치를 비동기적으로 수행합니다.

## 📊 서비스별 상세

### 🚪 API Gateway (8080)
- **역할**: 시스템의 단일 진입점(Single Point of Entry).
- **주요 기능**: Spring Cloud Gateway 기반 라우팅, JWT 인증 필터링, Refresh Token DB 검증 및 재발급.

### 🔐 User Service (8081)
- **역할**: 사용자 및 인증 관리의 핵심 서비스.
- **주요 기능**:
    - REST API 엔드포인트 제공 (회원가입, 로그인, 프로필 관리).
    - 다른 마이크로서비스(Notification, Board 등)를 호출하는 gRPC 클라이언트 역할 수행.

### 🗺️ Map Service (8082)
- **역할**: 위치 기반 정보 제공.
- **주요 기능**:
    - 위치 기반 정보 검색.
    - **gRPC 서비스**: `ReverseGeocodingService` 제공.

### 📝 Board Service (8083)
- **역할**: 커뮤니티 게시판 기능 제공.
- **주요 기능**:
    - 게시글 및 댓글 CRUD.
    - **gRPC 서비스**: `BoardService` 제공.

### 📸 Photo Service (8084)
- **역할**: 사진 업로드 및 관리.
- **주요 기능**:
    - 파일 업로드 처리 (현재는 로컬 저장, 향후 S3 연동).
    - **gRPC 서비스**: `FileDeleteService` 제공.

### 🔔 Notification Service (8085)
- **역할**: 비동기 알림 처리.
- **주요 기능**:
    - Kafka 메시지를 구독하여 이메일, 푸시 알림 등 발송.
    - **gRPC 서비스**: `MailAuthRequestService`, `TelAuthRequestService` 제공.

## ⚙️ 로컬 환경 설정

### 1. 요구사항
- **Java 21**
- **Docker & Docker Compose**

### 2. 환경 변수 설정
프로젝트를 실행하기 전에, 프로젝트 루트 디렉터리에 `.env.docker` 파일을 생성해야 합니다. 아래는 예시 템플릿입니다. **민감한 정보(예: `JWT_SECRET_KEY`, `KAKAO_CLIENT_*`)는 반드시 자신만의 값으로 변경하세요.**

```env
# .env.docker

# Service Ports
API_GATEWAY_SERVICE_PORT=8080
USER_SERVICE_PORT=8081
MAP_SERVICE_PORT=8082
BOARD_SERVICE_PORT=8083
PHOTO_SERVICE_PORT=8084
NOTIFICATION_SERVICE_PORT=8085

# Service URIs (for Docker Compose internal network)
USER_SERVICE_URI=http://user-server
MAP_SERVICE_URI=http://map-server
BOARD_SERVICE_URI=http://board-server
PHOTO_SERVICE_URI=http://photo-server
NOTIFICATION_SERVICE_URI=http://notification-server

# Database
DATABASE_HOST=db
DATABASE_PORT=5432
DATABASE_NAME=unimal
DATABASE_USERNAME=unimal
DATABASE_PASSWORD=unimalpw
DATABASE_USER_SCHEMA=user_schema

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redispw

# JWT Secret Key (Base64 Encoded) - Use a strong, unique key
JWT_SECRET_KEY=YW5kMC1zZWNyZXQta2V5LWp3dC1zZWNyZXQta2V5LWp3dC1zZWNyZXQta2V5

# OAuth (Example for Kakao) - Replace with your own keys
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

## 🚀 프로젝트 시작하기

1.  **리포지토리 클론**:
    ```bash
    git clone https://github.com/your-username/unimal-server.git
    cd unimal-server
    ```

2.  **환경 변수 파일 생성**:
    위의 [환경 변수 설정](#2-환경-변수-설정) 섹션을 참고하여 `.env.docker` 파일을 생성합니다.

3.  **프로젝트 빌드 및 Docker 이미지 생성**:
    Gradle Wrapper를 사용하여 전체 프로젝트를 빌드하고 각 서비스의 Docker 이미지를 생성합니다.
    ```bash
    ./gradlew clean build bootBuildImage
    ```

4.  **Docker Compose를 이용한 전체 서비스 실행**:
    데이터베이스, 캐시, 메시지 브로커 및 모든 마이크로서비스를 Docker Compose로 한 번에 실행합니다.
    ```bash
    docker-compose -f docker-compose.yml -f docker-compose-kafka.yml up -d
    ```

5.  **서비스 확인**:
    모든 컨테이너가 정상적으로 실행되었는지 확인합니다.
    ```bash
    docker-compose ps
    ```
    API Gateway는 `http://localhost:8080`에서 접근 가능합니다.

---

<div align="center">

**🐾 Unimal - 우리 주변 동물 🐾**

Made with ❤️ by Kane

</div>