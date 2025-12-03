# mmb 프로젝트 구조 문서

이 문서는 `mmb` (My Memory Booster) 프로젝트의 디렉토리 및 패키지 구조를 설명합니다.

## 프로젝트 개요
- **ArtifactId**: `mmb`
- **GroupId**: `com.mmb`
- **기반 기술**: Java 17, Spring Boot 3.5.8, Maven
- **주요 의존성**: Spring Web/WebFlux, Data JPA, MyBatis, MySQL, Security, WebSocket, JSP (Tomcat Embed Jasper)

## 디렉토리 구조

```
mmb/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mmb/          # 메인 패키지
│   │   │           ├── api/      # 외부 API 연동 (DictionaryAPI, DeepL 등)
│   │   │           ├── config/   # Spring 설정 클래스
│   │   │           ├── controller/# 웹 컨트롤러 (요청 처리)
│   │   │           ├── dao/      # MyBatis DAO (Data Access Object)
│   │   │           ├── dto/      # DTO (Data Transfer Object)
│   │   │           ├── entity/   # JPA 엔티티 클래스
│   │   │           ├── interceptor/# 웹 인터셉터
│   │   │           ├── legacy/   # 레거시 코드 보관
│   │   │           ├── repository/# JPA 리포지토리
│   │   │           ├── scheduler/# 스케줄러 작업
│   │   │           ├── service/  # 비즈니스 로직 서비스
│   │   │           ├── util/     # 유틸리티 클래스
│   │   │           └── MmbApplication.java # 메인 실행 클래스
│   │   ├── resources/
│   │   │   ├── static/           # 정적 리소스 (JS, CSS, Images)
│   │   │   ├── application.properties # 애플리케이션 설정
│   │   │   └── data.sql          # 초기 데이터 SQL
│   │   └── webapp/
│   │       └── view/             # JSP 뷰 파일
│   └── test/                     # 테스트 코드
├── pom.xml                       # Maven 빌드 설정
└── mvnw, mvnw.cmd                # Maven Wrapper
```

## 주요 패키지 설명

### `com.mmb`
애플리케이션의 루트 패키지입니다.

- **`api`**: 외부 서비스와의 통신을 담당하는 클래스들이 위치합니다. (예: 번역 서비스, 사전 API 등)
- **`config`**: `SecurityConfig`, `WebConfig` 등 프로젝트 전반의 설정을 담당하는 클래스들이 위치합니다.
- **`controller`**: 사용자 요청을 받아 서비스 계층으로 전달하고, 결과를 뷰나 JSON으로 반환하는 컨트롤러들이 위치합니다.
- **`dao`**: MyBatis를 사용하는 경우, 데이터베이스 접근을 위한 Mapper 인터페이스나 DAO 클래스가 위치합니다.
- **`dto`**: 계층 간 데이터 교환을 위한 객체들이 위치합니다.
- **`entity`**: 데이터베이스 테이블과 매핑되는 JPA 엔티티 클래스들이 위치합니다.
- **`repository`**: Spring Data JPA를 사용하여 데이터베이스에 접근하는 인터페이스들이 위치합니다.
- **`service`**: 실제 비즈니스 로직을 수행하는 서비스 클래스들이 위치합니다.
- **`scheduler`**: 주기적인 작업을 처리하는 스케줄러 클래스들이 위치합니다.
- **`legacy`**: 더 이상 사용되지 않거나 참고용으로 남겨둔 코드들이 위치합니다.

## 리소스 및 웹앱

- **`src/main/resources`**: 자바 소스 외의 설정 파일이나 정적 리소스가 위치합니다. `application.properties`에서 DB 연결 정보 등을 설정합니다.
- **`src/main/webapp/view`**: JSP 파일들이 위치하는 곳으로, 서버 사이드 렌더링을 위한 뷰 템플릿들이 저장됩니다.
