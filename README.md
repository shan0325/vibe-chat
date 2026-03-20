# Chat

Spring Boot 기반의 실시간 웹 채팅 서비스입니다.  
헥사고날 아키텍처(Ports & Adapters)와 Gradle 멀티모듈 구조로 설계되었습니다.

---

## 기능

- 최초 접속 시 랜덤 닉네임 자동 발급
- 닉네임 직접 변경 가능
- 메인 페이지에서 전체 접속자와 공용 채팅
- 실시간 접속자 목록 표시
- 채팅방 생성 및 입장
- 방 단위 1:N 실시간 채팅
- 참여자 목록 실시간 갱신
- 입장 / 퇴장 시스템 메시지

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.3 |
| WebSocket | Spring WebSocket / STOMP |
| Persistence | Spring Data JPA, Hibernate |
| Query | Querydsl 5.1.0 |
| Database | H2 (In-Memory) |
| View | Thymeleaf |
| Frontend | jQuery, Bootstrap 5, SockJS, STOMP.js |
| Build | Gradle 멀티모듈 |

---

## 모듈 구조

```
chat
├── chat-bootstrap       # Spring Boot 실행 진입점, 모든 모듈 조립
├── chat-domain          # 순수 도메인 모델 (프레임워크 의존 없음)
├── chat-application     # 유스케이스, In/Out 포트 정의, 서비스
├── chat-adapter
│   ├── web              # MVC Controller, Thymeleaf 뷰
│   ├── websocket        # STOMP Controller, WebSocket 설정, 이벤트 처리
│   └── persistence      # JPA Entity, Repository, Querydsl
└── chat-common          # 공통 예외, 공통 유틸
```

### 모듈 의존 방향

```
chat-bootstrap
  └─► chat-adapter:web
  └─► chat-adapter:websocket
  └─► chat-adapter:persistence
        └─► chat-application
              └─► chat-domain
                    └─► chat-common
```

> 도메인은 바깥 계층에 의존하지 않는다.

---

## 아키텍처

헥사고날 아키텍처(Ports & Adapters)를 적용합니다.

```
           ┌──────────────────────────────────────┐
           │            chat-adapter              │
           │  ┌────────┐  ┌─────────┐  ┌───────┐ │
           │  │  web   │  │websocket│  │persist│ │
           │  └───┬────┘  └────┬────┘  └───┬───┘ │
           └──────┼────────────┼────────────┼─────┘
                  │            │            │
           ┌──────▼────────────▼────────────▼─────┐
           │          chat-application             │
           │   UseCase / Port(in) / Port(out)      │
           └──────────────────┬───────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │    chat-domain    │
                    │  순수 도메인 모델  │
                    └───────────────────┘
```
---

## WebSocket / STOMP 설계

### Endpoint
```
ws://localhost:8080/ws-chat
```

### 클라이언트 → 서버 (publish)

| Destination | 설명 |
|---|---|
| `/pub/lobby/message` | 전체 채팅 메시지 전송 |
| `/pub/room/{roomId}/message` | 방 채팅 메시지 전송 |

### 서버 → 클라이언트 (subscribe)

| Destination | 설명 |
|---|---|
| `/topic/lobby` | 전체 채팅 메시지 수신 |
| `/topic/presence` | 접속자 목록 갱신 |
| `/topic/rooms` | 방 목록 갱신 |
| `/topic/room/{roomId}` | 방 채팅 메시지 수신 |
| `/topic/room/{roomId}/participants` | 방 참여자 목록 갱신 |

---

## DB 테이블

| 테이블 | 설명 |
|---|---|
| `member_profile` | 사용자 정보 (UUID, 닉네임) |
| `lobby_message` | 전체 채팅 메시지 이력 |
| `chat_room` | 채팅방 정보 |
| `room_participant` | 방 참여자 (복합키: room_id + member_id) |
| `room_message` | 방 채팅 메시지 이력 |

> H2 인메모리 DB를 사용하며 애플리케이션 재시작 시 초기화됩니다.

---

## REST API

### 사용자

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/` | 진입 페이지 (랜덤 닉네임 발급) |
| `POST` | `/nickname` | 닉네임 설정 후 메인으로 이동 |
| `GET` | `/main` | 메인 채팅 페이지 |
| `GET` | `/api/members/online` | 현재 접속자 목록 조회 |

### 로비

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/lobby/history` | 전체 채팅 최근 메시지 조회 |

### 채팅방

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/rooms` | 방 목록 조회 |
| `POST` | `/api/rooms` | 방 생성 |
| `GET` | `/room/{roomId}` | 방 채팅 페이지 (자동 입장) |
| `POST` | `/api/rooms/{roomId}/leave` | 방 퇴장 |
| `GET` | `/api/rooms/{roomId}/history` | 방 채팅 이력 조회 |
| `GET` | `/api/rooms/{roomId}/participants` | 방 참여자 목록 조회 |

---

## 실행 방법

### 요구사항
- Java 17 이상

### 실행

```bash
./gradlew :chat-bootstrap:bootRun
```

이후 브라우저에서 접속합니다.

```
http://localhost:8080
```

---

## 화면 흐름

```
[진입 페이지 /]
  └─ 랜덤 닉네임 표시 및 수정
  └─ 입장 버튼 클릭
        │
        ▼
[메인 페이지 /main]
  ├─ 전체 채팅 (STOMP /topic/lobby)
  ├─ 접속자 목록 (STOMP /topic/presence)
  ├─ 방 목록 (STOMP /topic/rooms)
  └─ 방 만들기 → [방 채팅 페이지 /room/{roomId}]
                    ├─ 방 메시지 (STOMP /topic/room/{id})
                    ├─ 참여자 목록 (STOMP /topic/room/{id}/participants)
                    └─ 나가기 → 메인으로 복귀
```

---

## 개발 현황

| Phase | 내용 | 상태 |
|---|---|---|
| Phase 0 | 멀티모듈 / 헥사고날 구조 구성 | ✅ 완료 |
| Phase 1 | 사용자 진입 / 닉네임 기능 | ✅ 완료 |
| Phase 2 | 메인 페이지 / 전체 채팅 | ✅ 완료 |
| Phase 3 | 방 생성 / 방 목록 / 1:N 채팅 | ✅ 완료 |
| Phase 4 | 1:1 채팅 | 🔜 예정 |
| Phase 5 | 영속화 / Querydsl 고도화 | 🔜 예정 |
| Phase 6 | 안정화 / 테스트 / 운영 준비 | 🔜 예정 |

