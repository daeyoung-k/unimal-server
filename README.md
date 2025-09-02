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

### 기술 스택

**Backend Framework**
- **Kotlin** + **Spring Boot** + **JPA**
- **Java 21** + **Gradle Kotlin DSL**

**Communication**
- **REST API** - 클라이언트 통신
- **gRPC** - 마이크로서비스 간 통신
- **Apache Kafka** - 이벤트 기반 비동기 처리

**Database & Cache**
- **PostgreSQL** - 주 데이터베이스
- **Redis** - 캐싱 및 세션 관리

**Infrastructure**
- **Docker** + **Docker Compose** - 컨테이너화
- **AWS S3** - 파일 저장소
- **Spring Cloud Gateway** - API 라우팅

## 🔧 주요 기술적 특징

### 1. 마이크로서비스 간 통신
- **gRPC**: 높은 성능과 타입 안정성을 위한 서비스 간 통신
- **Protocol Buffers**: 효율적인 데이터 직렬화
- **Service Discovery**: Spring Cloud Gateway를 통한 라우팅

### 2. 이벤트 드리븐 아키텍처
- **Apache Kafka**: 비동기 이벤트 처리
- **Event Sourcing**: 도메인 이벤트 기반 상태 관리

### 3. 보안
- **JWT**: 무상태 인증 토큰
- **Spring Security**: 엔드포인트 보안
- **CORS**: 크로스 오리진 요청 처리

### 4. 확장성 & 성능
- **Connection Pooling**: 데이터베이스 연결 최적화
- **Redis Caching**: 응답 속도 향상
- **Docker**: 컨테이너 기반 배포

## 📊 서비스별 상세

### 🔐 User Service (8081)
- 회원가입, 로그인, JWT 토큰 관리
- 사용자 프로필 관리
- 권한 및 역할 기반 접근 제어

### 🗺️ Map Service (8082)
- 위치 기반 정보 검색
- 지도 API 연동

### 📝 Board Service (8083)
- 게시글 CRUD
- 댓글 시스템
- 카테고리 기반 분류

### 📸 Photo Service (8084)
- AWS S3 연동 파일 업로드
- 멀티파트 파일 업로드 지원

### 🔔 Notification Service (8085)
- Kafka 기반 실시간 알림
- 이메일, 푸시 알림 발송
- 알림 설정 관리

## 🛠️ 개발 환경

### 요구사항
- **Java 21**
- **Kotlin 1.9.25**
- **Docker & Docker Compose**
- **PostgreSQL 13+**
- **Redis 6+**

---

<div align="center">

**🐾 Unimal - 우리 주변 동물 🐾**

Made with ❤️ by Kane

</div>