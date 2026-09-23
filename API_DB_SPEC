# FocusGrow DB & API Specification

> ADHD 스터디 플래너 앱의 초기 DB 설계 및 REST API 명세서  
> 대상 화면: 로그인/회원가입 → 목표 설정/기록 조회/MY

---

## 1. 화면 및 기능 흐름

```text
[첫 화면]
 ├─ 회원가입
 └─ 로그인
       │
       ▼
[메인 화면]
 ├─ 목표 설정
 ├─ 기록 조회
 └─ MY
```

목표를 등록하면 목표 기간과 분량을 기준으로 하루 단위의 일정(Schedule)을 생성한다.

```text
회원가입/로그인
      ↓
사용자(USER)
      ↓
목표(GOAL) 생성
      ↓
일일 일정(SCHEDULE) 생성
      ↓
일정 완료
      ↓
기록(RECORD) 저장
      ↓
캐릭터 경험치/레벨 반영
```

---

# 2. ERD

## 2.1 테이블 관계

```text
┌───────────────┐
│     USER      │
├───────────────┤
│ PK user_id    │
│ email         │
│ password      │
│ nickname      │
│ created_at    │
└───────┬───────┘
        │ 1:N
        ▼
┌───────────────┐
│      GOAL     │
├───────────────┤
│ PK goal_id    │
│ FK user_id    │
│ title         │
│ description   │
│ start_date    │
│ end_date      │
│ total_amount  │
│ unit          │
│ status        │
│ created_at    │
└───────┬───────┘
        │ 1:N
        ▼
┌───────────────┐
│   SCHEDULE    │
├───────────────┤
│ PK schedule_id│
│ FK goal_id    │
│ date          │
│ title         │
│ amount        │
│ start_time    │
│ end_time      │
│ completed     │
│ completed_at  │
└───────┬───────┘
        │ 1:1
        ▼
┌───────────────┐
│    RECORD     │
├───────────────┤
│ PK record_id  │
│ FK user_id    │
│ FK schedule_id│
│ completed_at  │
│ study_time    │
└───────────────┘

┌───────────────┐
│   CHARACTER   │
├───────────────┤
│ PK character_id│
│ FK user_id    │
│ level         │
│ experience    │
└───────────────┘
```

### 관계 설명

- USER 1 : N GOAL
  - 한 명의 사용자는 여러 개의 목표를 가질 수 있다.
- GOAL 1 : N SCHEDULE
  - 하나의 목표는 여러 날짜의 일정으로 나뉜다.
- SCHEDULE 1 : 1 RECORD
  - 완료된 일정에 대해 기록을 생성한다.
- USER 1 : 1 CHARACTER
  - 한 명의 사용자에게 하나의 캐릭터가 연결된다.

---

# 3. DB 설계

## 3.1 USER

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| user_id | BIGINT | PK, AUTO_INCREMENT | 사용자 ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 로그인 이메일 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| nickname | VARCHAR(50) | NOT NULL | 사용자 닉네임 |
| created_at | DATETIME | NOT NULL | 가입일 |

## 3.2 GOAL

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| goal_id | BIGINT | PK, AUTO_INCREMENT | 목표 ID |
| user_id | BIGINT | FK, NOT NULL | 목표를 생성한 사용자 |
| title | VARCHAR(255) | NOT NULL | 목표 이름 |
| description | TEXT | NULL | 목표 설명 |
| start_date | DATE | NOT NULL | 목표 시작일 |
| end_date | DATE | NOT NULL | 목표 종료일 |
| total_amount | INT | NOT NULL | 전체 분량 |
| unit | VARCHAR(30) | NOT NULL | 분량 단위 (page, count, minute 등) |
| status | VARCHAR(30) | NOT NULL | IN_PROGRESS / COMPLETED / CANCELLED |
| created_at | DATETIME | NOT NULL | 목표 생성일 |

## 3.3 SCHEDULE

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| schedule_id | BIGINT | PK, AUTO_INCREMENT | 일정 ID |
| goal_id | BIGINT | FK, NOT NULL | 연결된 목표 |
| date | DATE | NOT NULL | 일정 날짜 |
| title | VARCHAR(255) | NOT NULL | 오늘 할 일 |
| amount | INT | NULL | 오늘 수행할 분량 |
| start_time | TIME | NULL | 시작 시간 |
| end_time | TIME | NULL | 종료 시간 |
| completed | BOOLEAN | NOT NULL, DEFAULT FALSE | 완료 여부 |
| completed_at | DATETIME | NULL | 완료 시간 |

## 3.4 RECORD

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| record_id | BIGINT | PK, AUTO_INCREMENT | 기록 ID |
| user_id | BIGINT | FK, NOT NULL | 사용자 ID |
| schedule_id | BIGINT | FK, NOT NULL | 완료한 일정 |
| completed_at | DATETIME | NOT NULL | 완료 시간 |
| study_time | INT | NULL | 실제 공부 시간(분) |

## 3.5 CHARACTER

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| character_id | BIGINT | PK, AUTO_INCREMENT | 캐릭터 ID |
| user_id | BIGINT | FK, UNIQUE, NOT NULL | 사용자 ID |
| level | INT | NOT NULL, DEFAULT 1 | 캐릭터 레벨 |
| experience | INT | NOT NULL, DEFAULT 0 | 경험치 |

---

# 4. REST API 공통 규칙

## Base URL

개발 환경 예시:

```text
http://localhost:8080
```

운영 환경에서는 AWS 배포 후 실제 도메인으로 변경한다.

## 인증

로그인 성공 시 JWT Access Token을 발급한다.

인증이 필요한 API는 아래 Header를 사용한다.

```http
Authorization: Bearer {accessToken}
```

### 인증 필요 여부

| API | 인증 |
|---|---|
| 회원가입 | X |
| 로그인 | X |
| 내 정보 조회 | O |
| 목표 관련 API | O |
| 일정 관련 API | O |
| 기록 관련 API | O |
| 캐릭터 관련 API | O |

---

# 5. API 명세

## 5.1 회원가입

### POST `/api/auth/signup`

회원가입을 처리한다.

### Request

```json
{
  "email": "test@example.com",
  "password": "12345678",
  "nickname": "홍길동"
}
```

### Response `201 Created`

```json
{
  "userId": 1,
  "email": "test@example.com",
  "nickname": "홍길동"
}
```

---

## 5.2 로그인

### POST `/api/auth/login`

로그인 후 JWT Access Token을 발급한다.

### Request

```json
{
  "email": "test@example.com",
  "password": "12345678"
}
```

### Response `200 OK`

```json
{
  "accessToken": "JWT_ACCESS_TOKEN",
  "user": {
    "userId": 1,
    "nickname": "홍길동"
  }
}
```

---

# 6. MY

## 6.1 내 정보 조회

### GET `/api/users/me`

현재 로그인한 사용자의 정보를 조회한다.

### Response `200 OK`

```json
{
  "userId": 1,
  "email": "test@example.com",
  "nickname": "홍길동",
  "createdAt": "2026-09-23T10:00:00"
}
```

---

# 7. 목표 설정

## 7.1 목표 생성

### POST `/api/goals`

사용자가 새로운 목표를 생성한다.

### Request

```json
{
  "title": "책 2권 읽기",
  "description": "두 달 동안 책 2권 완독",
  "startDate": "2026-10-01",
  "endDate": "2026-11-30",
  "totalAmount": 600,
  "unit": "page"
}
```

### Response `201 Created`

```json
{
  "goalId": 1,
  "title": "책 2권 읽기",
  "startDate": "2026-10-01",
  "endDate": "2026-11-30",
  "totalAmount": 600,
  "unit": "page",
  "dailyAmount": 10,
  "status": "IN_PROGRESS"
}
```

> `dailyAmount`는 목표 기간을 기준으로 계산하는 값이다.
> 실제 자동 일정 분배 알고리즘의 상세 규칙은 별도로 정의한다.

---

## 7.2 목표 목록 조회

### GET `/api/goals`

현재 로그인한 사용자의 목표 목록을 조회한다.

### Response `200 OK`

```json
[
  {
    "goalId": 1,
    "title": "책 2권 읽기",
    "startDate": "2026-10-01",
    "endDate": "2026-11-30",
    "progress": 35,
    "status": "IN_PROGRESS"
  }
]
```

---

## 7.3 목표 상세 조회

### GET `/api/goals/{goalId}`

### Response `200 OK`

```json
{
  "goalId": 1,
  "title": "책 2권 읽기",
  "description": "두 달 동안 책 2권 완독",
  "startDate": "2026-10-01",
  "endDate": "2026-11-30",
  "totalAmount": 600,
  "completedAmount": 210,
  "progress": 35,
  "dailyAmount": 10,
  "status": "IN_PROGRESS"
}
```

---

## 7.4 목표 수정

### PATCH `/api/goals/{goalId}`

### Request

```json
{
  "title": "책 3권 읽기",
  "endDate": "2026-12-15"
}
```

### Response `200 OK`

```json
{
  "goalId": 1,
  "title": "책 3권 읽기",
  "endDate": "2026-12-15",
  "status": "IN_PROGRESS"
}
```

---

## 7.5 목표 삭제

### DELETE `/api/goals/{goalId}`

### Response `200 OK`

```json
{
  "message": "목표가 삭제되었습니다."
}
```

---

# 8. 일정

## 8.1 오늘 일정 조회

### GET `/api/schedules/today`

현재 로그인한 사용자의 오늘 일정을 조회한다.

### Response `200 OK`

```json
{
  "date": "2026-10-01",
  "schedules": [
    {
      "scheduleId": 1,
      "goalId": 1,
      "title": "책 10페이지 읽기",
      "amount": 10,
      "startTime": "19:00",
      "endTime": "19:30",
      "completed": false
    }
  ]
}
```

---

## 8.2 일정 완료

### PATCH `/api/schedules/{scheduleId}/complete`

사용자가 오늘의 일정을 완료 처리한다.

### Response `200 OK`

```json
{
  "scheduleId": 1,
  "completed": true,
  "completedAt": "2026-10-01T19:32:00"
}
```

완료 처리 시 다음 작업을 연동할 수 있다.

```text
일정 완료
  ↓
SCHEDULE.completed = true
  ↓
RECORD 생성
  ↓
CHARACTER 경험치 증가
  ↓
레벨업 여부 확인
```

---

# 9. 기록 조회

## 9.1 기록 조회

### GET `/api/records`

사용자의 공부/목표 수행 기록을 조회한다.

### Response `200 OK`

```json
{
  "totalGoals": 3,
  "completedGoals": 1,
  "totalStudyTime": 1250,
  "completionRate": 82,
  "records": [
    {
      "date": "2026-10-01",
      "completedCount": 4,
      "totalCount": 5,
      "studyTime": 80
    }
  ]
}
```

---

## 9.2 월별 기록 조회

### GET `/api/records/monthly?year=2026&month=10`

캘린더 형태의 기록 화면에서 사용한다.

### Response `200 OK`

```json
{
  "year": 2026,
  "month": 10,
  "records": [
    {
      "date": "2026-10-01",
      "completionRate": 80
    },
    {
      "date": "2026-10-02",
      "completionRate": 100
    }
  ]
}
```

---

# 10. 캐릭터

## 10.1 캐릭터 조회

### GET `/api/character`

### Response `200 OK`

```json
{
  "characterId": 1,
  "level": 3,
  "experience": 240,
  "nextLevelExperience": 300
}
```

---

## 10.2 캐릭터 성장 기록 조회

### GET `/api/character/history`

캐릭터의 성장 내역을 조회한다.

### Response `200 OK`

```json
{
  "history": [
    {
      "date": "2026-10-01",
      "experience": 10,
      "reason": "일정 완료"
    }
  ]
}
```

---

# 11. API 전체 목록

| 기능 | Method | Endpoint | 인증 |
|---|---|---|---|
| 회원가입 | POST | `/api/auth/signup` | X |
| 로그인 | POST | `/api/auth/login` | X |
| 내 정보 조회 | GET | `/api/users/me` | O |
| 목표 생성 | POST | `/api/goals` | O |
| 목표 목록 조회 | GET | `/api/goals` | O |
| 목표 상세 조회 | GET | `/api/goals/{goalId}` | O |
| 목표 수정 | PATCH | `/api/goals/{goalId}` | O |
| 목표 삭제 | DELETE | `/api/goals/{goalId}` | O |
| 오늘 일정 조회 | GET | `/api/schedules/today` | O |
| 일정 완료 | PATCH | `/api/schedules/{scheduleId}/complete` | O |
| 기록 조회 | GET | `/api/records` | O |
| 월별 기록 조회 | GET | `/api/records/monthly` | O |
| 캐릭터 조회 | GET | `/api/character` | O |
| 캐릭터 성장 기록 | GET | `/api/character/history` | O |

---

# 12. 개발 우선순위

## 1주차

```text
1. ERD 확정
2. USER / GOAL / SCHEDULE 테이블 설계
3. 프로젝트 DB 연결
4. API 기본 구조 생성
5. Swagger/OpenAPI 설정
6. 회원가입 API
7. 로그인 API
8. 목표 CRUD
```

## 2주차 이후

```text
일정 자동 분배
    ↓
SCHEDULE API
    ↓
일정 완료
    ↓
RECORD
    ↓
CHARACTER 성장
    ↓
프론트엔드 연동
```

---

# 13. 개발 시 확인할 사항

- 비밀번호는 DB에 평문으로 저장하지 않는다.
- 로그인 인증은 JWT를 사용하는 것을 기본으로 한다.
- 사용자별 데이터는 반드시 로그인한 사용자의 `user_id`를 기준으로 조회한다.
- `goalId`, `scheduleId` 등 다른 사용자의 리소스에 접근하지 못하도록 서버에서 권한을 확인한다.
- API Request/Response 형식이 변경되면 팀원에게 공유하고 이 문서도 함께 수정한다.
- 자동 일정 분배 알고리즘의 상세 규칙은 API 담당자와 알고리즘 담당자가 협의하여 별도 문서화한다.

---

# 14. GitHub 권장 폴더 구조

```text
FocusGrow/
├── README.md
├── docs/
│   ├── API_DB_SPEC.md
│   └── ERD.png
├── backend/
├── frontend/
└── ...
```

`API_DB_SPEC.md`에는 이 문서를 넣고, ERD를 이미지로 따로 만들어 `docs/ERD.png`로 관리하는 것을 권장한다.
