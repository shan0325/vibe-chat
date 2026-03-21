# Chat 프로젝트 계획서

## 1. 프로젝트 개요

이 프로젝트는 웹 브라우저에서 사용할 수 있는 실시간 채팅 서비스 구현을 목표로 한다.

핵심 기능은 다음과 같다.

- 최초 접속 시 랜덤 닉네임 자동 부여
- 사용자가 직접 닉네임 변경 가능
- 메인 페이지에서 전체 접속자와 공용 채팅 가능
- 채팅방 생성 가능
- 방 단위 1:N 채팅 가능
- 특정 사용자와 1:1 채팅 가능

예정 기술 스택은 다음과 같다.

- Spring Boot
- Spring WebSocket / STOMP
- Spring Data JPA
- Querydsl
- H2 Database
- Thymeleaf
- jQuery
- Gradle 멀티모듈
- 헥사고날 아키텍처

---

## 2. 현재 상태

현재 프로젝트는 단일 모듈의 Spring Boot 시작 상태이다.

- `build.gradle`: 기본 Spring Boot 프로젝트
- `settings.gradle`: 루트 프로젝트 이름만 정의
- `src/main/java/com/shan/chat/ChatApplication.java`: 애플리케이션 진입점
- `src/main/resources/application.properties`: 애플리케이션 이름만 설정

즉, 현재는 기능 구현 전의 초기 템플릿 상태이며, 본격적인 개발에 앞서 멀티모듈 구조와 아키텍처 경계를 먼저 정리하는 것이 적절하다.

---

## 3. 목표 아키텍처

### 3.1 아키텍처 방향

본 프로젝트는 **헥사고날 아키텍처**를 적용한다.

핵심 원칙은 다음과 같다.

- 도메인 로직은 프레임워크에 의존하지 않는다.
- 애플리케이션 서비스는 유스케이스 중심으로 구성한다.
- 웹, 웹소켓, DB는 모두 어댑터로 분리한다.
- 외부 기술은 안쪽 계층을 참조할 수 있지만, 도메인은 바깥 계층을 몰라야 한다.

### 3.2 추천 멀티모듈 구조

```text
chat
├── chat-bootstrap                # 실행 모듈, Spring Boot Application, 설정 조립
├── chat-domain                   # 엔티티, 값 객체, 도메인 서비스, 도메인 규칙
├── chat-application              # 유스케이스, 포트(in/out), DTO, 트랜잭션 경계
├── chat-adapter
│   ├── web                       # MVC Controller, Thymeleaf View
│   ├── websocket                 # STOMP Controller, WebSocket 설정, 세션 처리
│   └── persistence               # JPA Entity, Repository 구현체, Querydsl
└── chat-common                   # 공통 예외, 공통 응답, 유틸(필요 시)
```

Gradle 멀티모듈 기준으로는 `:chat-adapter:web`, `:chat-adapter:websocket`, `:chat-adapter:persistence` 형태를 사용한다.

### 3.3 모듈별 역할

#### `chat-bootstrap`
- Spring Boot 실행 모듈
- 각 모듈 Bean 조립
- 환경설정 로딩
- 실행 profile 관리

#### `chat-domain`
- `ChatRoom`, `ChatMessage`, `MemberProfile`, `Participant` 등 핵심 모델
- 채팅방 생성 규칙, 입장/퇴장 규칙, 닉네임 규칙
- 메시지 발송 가능 여부 등의 도메인 규칙

#### `chat-application`
- 유스케이스 중심 서비스
- 예시:
  - `GenerateRandomNicknameUseCase`
  - `ChangeNicknameUseCase`
  - `CreateRoomUseCase`
  - `JoinRoomUseCase`
  - `SendLobbyMessageUseCase`
  - `SendRoomMessageUseCase`
  - `SendDirectMessageUseCase`
- In/Out Port 정의

#### `chat-adapter`
- 어댑터 모듈을 묶는 상위 모듈
- 인바운드/아웃바운드 어댑터를 역할별 하위 모듈로 분리
- 권장 하위 모듈: `web`, `websocket`, `persistence`

#### `chat-adapter:web`
- 최초 진입 페이지
- 닉네임 설정 페이지
- 메인 페이지
- 방 생성/입장 화면
- Thymeleaf + jQuery 기반 UI

#### `chat-adapter:websocket`
- STOMP endpoint 설정
- 메시지 수신 컨트롤러
- 세션 연결/해제 이벤트 처리
- 사용자별 destination 관리

#### `chat-adapter:persistence`
- JPA Entity 및 Repository 구현
- Querydsl 기반 조회
- H2 DB 설정
- 메시지/방/참여자 저장

#### `chat-common`
- 공통 예외
- 공통 상수
- 공통 유틸
- 너무 많은 역할이 몰리지 않도록 최소화

---

## 4. 기능 요구사항 정리

### 4.1 사용자 진입
- 사용자가 처음 페이지에 들어오면 랜덤 닉네임을 발급한다.
- 사용자는 원하는 닉네임으로 변경할 수 있다.
- 닉네임 설정 후 메인 페이지로 진입한다.

### 4.2 메인 페이지
- 전체 접속자 목록을 확인할 수 있다.
- 메인 페이지에서 전체 사용자 대상 공용 채팅이 가능하다.
- 생성된 채팅방 목록을 확인할 수 있다.
- 새 채팅방을 만들 수 있다.

### 4.3 채팅방
- 방 제목을 입력해 방 생성 가능
- 방 참여자 목록 표시
- 방 내부에서 1:N 채팅 가능
- 사용자는 방을 나갈 수 있다.

### 4.4 1:1 채팅
- 접속자 목록에서 특정 사용자를 선택해 1:1 채팅 가능
- 개인 채팅은 다른 사용자에게 노출되지 않아야 함
- 사용자 재접속/세션 종료 시 상태 정리가 필요함

---

## 5. 화면 흐름 초안

```text
[진입 페이지]
  -> 랜덤 닉네임 생성
  -> 닉네임 수정 가능
  -> 입장 버튼
      -> [메인 페이지]
            - 전체 채팅
            - 접속자 목록
            - 방 목록
            - 방 생성
                -> [방 채팅 페이지]
            - 사용자 선택
                -> [1:1 채팅 UI]
```

### 주요 페이지
1. `진입 페이지`
   - 랜덤 닉네임 표시
   - 닉네임 수정 입력창
   - 시작 버튼

2. `메인 페이지`
   - 내 닉네임
   - 전체 채팅 영역
   - 접속자 목록
   - 방 목록
   - 방 생성 폼

3. `방 채팅 페이지`
   - 방 제목
   - 참여자 목록
   - 메시지 목록
   - 메시지 입력창
   - 나가기 버튼

4. `1:1 채팅 UI`
   - 대상 닉네임
   - 개인 메시지 목록
   - 메시지 입력창

---

## 6. 도메인 초안

### 6.1 핵심 도메인 모델

#### `MemberProfile`
- 사용자 식별자
- 현재 닉네임
- 랜덤 생성 여부
- 접속 상태

#### `ChatRoom`
- 방 ID
- 방 이름
- 방 타입(LOBBY, GROUP, DIRECT)
- 생성자 ID
- 생성 시간
- 활성 여부

#### `RoomParticipant`
- 방 ID
- 사용자 ID
- 입장 시간
- 권한(방장 여부 등)

#### `ChatMessage`
- 메시지 ID
- 방 ID 또는 대상 사용자 ID
- 발신자 ID
- 메시지 타입(TEXT, SYSTEM)
- 메시지 내용
- 발송 시각

### 6.2 방 타입 제안
- `LOBBY`: 메인 페이지 전체 채팅
- `GROUP`: 일반 방 채팅(1:N)
- `DIRECT`: 1:1 채팅

---

## 7. WebSocket / STOMP 설계 초안

### 7.1 Endpoint
- `/ws-chat`

### 7.2 Client 송신 destination 예시
- `/pub/lobby/message`
- `/pub/room/{roomId}/message`
- `/pub/direct/{targetMemberId}/message`
- `/pub/nickname/change`
- `/pub/room/create`
- `/pub/room/{roomId}/join`
- `/pub/room/{roomId}/leave`

### 7.3 Client 구독 destination 예시
- `/topic/lobby`
- `/topic/rooms`
- `/topic/room/{roomId}`
- `/queue/direct`
- `/topic/presence`

### 7.4 주의 사항
- 사용자별 메시지는 `user destination` 사용을 우선 검토
- 세션 연결/종료 이벤트를 활용해 접속자 목록 갱신
- 닉네임 변경 시 메인/방/개인 채팅 화면에 즉시 반영

---

## 8. DB 설계 초안

초기에는 H2를 사용한다.

### 8.1 테이블 후보
- `member_profile`
- `chat_room`
- `room_participant`
- `chat_message`

### 8.2 초기 전략

초기 MVP에서는 아래와 같이 접근한다.

1. **접속 상태 / 현재 온라인 목록**: 메모리 + 세션 기반 관리
2. **방 정보 / 메시지 이력**: H2 저장
3. **향후 확장**: Redis 세션/브로커, 외부 DB(MySQL/PostgreSQL)로 전환 가능하게 설계

이 방식이면 실시간 상태 관리와 영속 데이터를 분리해서 단순하게 시작할 수 있다.

---

## 9. 패키지 구조 예시

### `chat-domain`
```text
com.shan.chat.domain
├── member
├── room
├── message
└── common
```

### `chat-application`
```text
com.shan.chat.application
├── port
│   ├── in
│   └── out
├── service
├── dto
└── mapper
```

### `chat-adapter`
```text
chat-adapter
├── web
├── websocket
└── persistence
```

### `chat-adapter:web`
```text
com.shan.chat.adapter.in.web
├── controller
├── view
└── dto
```

### `chat-adapter:websocket`
```text
com.shan.chat.adapter.in.websocket
├── config
├── controller
├── session
└── dto
```

### `chat-adapter:persistence`
```text
com.shan.chat.adapter.out.persistence
├── entity
├── repository
├── query
└── mapper
```

---

## 10. 단계별 개발 계획

## Phase 0. 프로젝트 기반 구성

### 목표
- 멀티모듈 구조 생성
- 헥사고날 계층 분리
- 기본 의존성 구성
- 개발 편의 설정 완료

### 작업 항목
- `settings.gradle`에 서브모듈 등록
- `chat-adapter` 하위에 `web`, `websocket`, `persistence` 모듈 구성
- 루트 Gradle 공통 설정 분리
- 각 모듈별 `build.gradle` 생성
- JPA, Thymeleaf, WebSocket, H2, Querydsl 의존성 설정
- 공통 테스트 전략 수립
- 기본 profile (`local`) 설정

### 산출물
- 멀티모듈 빌드 성공
- 애플리케이션 정상 실행
- 각 모듈 간 의존 방향 정리 문서

---

## Phase 1. 사용자 진입 / 닉네임 기능

### 목표
- 사용자가 진입하면 랜덤 닉네임을 받는다.
- 닉네임을 수정하고 메인 페이지로 들어간다.

### 작업 항목
- 랜덤 닉네임 생성 정책 정의
- 세션 기반 사용자 식별 처리
- 닉네임 변경 유스케이스 구현
- 진입 페이지/닉네임 페이지 구현
- 사용자 기본 상태 저장 구조 작성

### 산출물
- 랜덤 닉네임 자동 표시
- 닉네임 수정 기능 동작
- 닉네임 저장 후 메인 페이지 이동

---

## Phase 2. 메인 페이지 / 전체 채팅

### 목표
- 전체 접속자 대상 공용 채팅 구현
- 접속자 목록 실시간 반영

### 작업 항목
- STOMP 연결 구성
- 전체 채팅 메시지 송수신 구현
- 메인 페이지 UI 구성
- 접속/종료 이벤트 처리
- 접속자 목록 브로드캐스트
- 시스템 메시지(입장/퇴장) 처리

### 산출물
- 메인 페이지 실시간 전체 채팅
- 접속자 목록 표시
- 입장/퇴장 이벤트 반영

---

## Phase 3. 방 생성 / 방 목록 / 1:N 채팅

### 목표
- 사용자가 방을 생성하고 참여할 수 있다.
- 방 안에서 다수 사용자와 채팅할 수 있다.

### 작업 항목
- 방 생성 유스케이스 구현
- 방 목록 조회 구현
- 방 입장/퇴장 처리
- 방별 메시지 destination 분리
- 방 참여자 목록 갱신
- 방 페이지 UI 구현

### 산출물
- 방 생성 기능
- 방 목록 표시
- 방별 실시간 1:N 채팅
- 참여자 목록 확인

---

## Phase 4. 1:1 채팅

### 목표
- 특정 사용자와 개인 채팅이 가능해야 한다.

### 작업 항목
- 개인 채팅 시작 규칙 정의
- direct room 생성 여부 결정
  - 방식 A: 사용자쌍마다 고정 Direct Room 생성
  - 방식 B: 메시지 발신 시점에 동적 생성
- 사용자별 queue 구독 구성
- 개인 채팅 UI 구현
- 읽기 쉬운 대화 목록 정리

### 산출물
- 사용자 선택 후 1:1 채팅 가능
- 개인 메시지 비공개 전송 보장

---

## Phase 5. 영속화 / 조회 고도화 ✅ 완료

### 목표
- 메시지/방/참여 이력을 DB에 저장한다.
- Querydsl 기반 조회 기능을 추가한다.

### 작업 항목
- JPA Entity 설계 확정
- Repository 구현
- Querydsl 설정 및 QClass 생성 환경 구성
- 방 목록 정렬/검색 조회
- 최근 메시지 조회
- 사용자별 참여 이력 조회

### 산출물
- H2 기반 데이터 저장
- Querydsl 조회 적용
- 기본 이력 확인 가능

---

## Phase 6. 안정화 / 테스트 / 운영 준비 ✅ 완료

### 목표
- 기능 안정화와 테스트 자동화를 진행한다.

### 작업 항목
- 단위 테스트 작성
- 유스케이스 통합 테스트 작성
- WebSocket 메시징 테스트 작성
- 예외 처리 정책 정리
- 중복 닉네임 정책 정리
- 세션 종료/브라우저 새로고침 대응
- 로그 정책 정리

### 산출물
- 핵심 유스케이스 테스트
- WebSocket 통합 테스트
- 예외/경계 상황 대응

---

## 11. 구현 우선순위 제안

### 1순위: 사용자 경험 최소 완성(MVP)
- 랜덤 닉네임
- 닉네임 수정
- 메인 페이지 입장
- 전체 채팅
- 접속자 목록

### 2순위: 방 기반 채팅
- 방 생성
- 방 입장/퇴장
- 방 목록
- 1:N 채팅

### 3순위: 개인 채팅
- 사용자 선택
- 1:1 메시지
- direct room 관리

### 4순위: 영속화 고도화
- 메시지 저장
- Querydsl 조회
- 최근 채팅 이력

### 5순위: 안정화
- 테스트
- 예외 처리
- 성능/구조 개선

---

## 12. 개발 순서에 대한 권장 전략

이번 프로젝트는 **처음부터 멀티모듈 + 헥사고날 구조를 잡고 시작하는 방식**을 권장한다.

이유는 다음과 같다.

- 채팅 기능은 웹, 웹소켓, 세션, DB 관심사가 빠르게 섞이기 쉽다.
- 초기에 단일 모듈로 빠르게 만들면 이후 분리 비용이 커질 가능성이 높다.
- 방 채팅, 개인 채팅, 접속 상태 관리가 추가될수록 포트/어댑터 분리의 장점이 커진다.

다만 실제 구현은 아래 순서로 진행한다.

1. 멀티모듈 뼈대 생성
2. 진입/닉네임 기능 구현
3. 전체 채팅 구현
4. 방 채팅 구현
5. 1:1 채팅 구현
6. DB/조회 고도화
7. 테스트/정리

---

## 13. 기술 검토 포인트

### 13.1 Spring Boot 버전
현재 `build.gradle`에는 `Spring Boot 4.0.3`가 설정되어 있다.

프로젝트 진행 전 아래를 검토한다.

- Querydsl 및 기타 라이브러리 호환성
- 사용할 Spring ecosystem 버전 안정성
- 예제/자료 접근성

필요 시 안정적인 버전 라인으로 조정하는 것도 검토할 수 있다.

### 13.2 닉네임 중복 정책
아래 중 하나를 결정해야 한다.

- 닉네임 중복 허용
- 중복 불가
- 중복 시 숫자 suffix 자동 부여

권장안:
- 화면 표시용 닉네임은 중복 가능하게 두되,
- 내부 식별은 UUID 기반으로 분리

### 13.3 온라인 상태 저장 위치
- 현재 온라인 상태: 메모리 기반
- 영속 데이터: H2 기반

초기 개발 단계에서는 이 구조가 가장 단순하다.

---

## 14. 1차 구현 목표(MVP) 정의

1차 목표는 아래까지 완성하는 것이다.

- 랜덤 닉네임 발급
- 닉네임 수정
- 메인 페이지 진입
- 전체 채팅
- 접속자 목록 표시
- 방 생성
- 방 입장 후 1:N 채팅

즉, **1:1 채팅과 Querydsl 고도화는 2차 작업**으로 두고,
먼저 전체 구조와 핵심 흐름을 안정적으로 완성하는 것이 좋다.

---

## 15. 최종 정리

이 프로젝트는 실시간 채팅이라는 특성상 다음 3가지를 가장 먼저 안정적으로 잡아야 한다.

1. 사용자 식별과 닉네임 흐름
2. WebSocket/STOMP 메시지 흐름
3. 방/참여자/메시지 도메인 구조

따라서 개발은 다음 원칙으로 진행한다.

- 멀티모듈 + 헥사고날 구조를 먼저 만든다.
- MVP는 전체 채팅 + 방 채팅까지를 우선 완성한다.
- 이후 1:1 채팅, Querydsl, 테스트 고도화를 진행한다.

이 문서를 기준으로 다음 작업은 다음 순서가 적절하다.

1. 멀티모듈 프로젝트 구조 생성
2. 공통 의존성 및 모듈별 의존관계 설정
3. 진입/닉네임 페이지 구현
4. WebSocket 전체 채팅 연결
5. 방 생성 및 방 채팅 구현

