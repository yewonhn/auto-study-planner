# Backend

FocusGrow의 서버 및 API를 담당합니다.

## 담당 기능

- 회원가입 / 로그인
- JWT 인증
- 사용자 관리
- 목표 CRUD
- 목표 기반 일일 일정 생성
- 일정 완료 처리
- 학습 기록 저장 및 조회
- 캐릭터 경험치 / 레벨 관리
- Frontend API 제공
- Database 연동

## 주요 API

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/users/me`
- `POST /api/goals`
- `GET /api/goals`
- `GET /api/goals/{goalId}`
- `PATCH /api/goals/{goalId}`
- `DELETE /api/goals/{goalId}`
- `GET /api/schedules/today`
- `PATCH /api/schedules/{scheduleId}/complete`
- `GET /api/records`
- `GET /api/records/monthly`
- `GET /api/character`
- `GET /api/character/history`

상세 Request / Response 형식은 `docs/API_DB_SPEC.md`를 참고합니다.

## Database

Database 구조는 `database/schema.sql`을 기준으로 관리합니다.

주요 테이블:

- `USER`
- `GOAL`
- `SCHEDULE`
- `RECORD`
- `CHARACTER`

## 작업 규칙

1. API 변경 시 API 명세도 함께 수정합니다.
2. DB 구조 변경 시 `database/schema.sql`도 함께 수정합니다.
3. 비밀번호는 평문으로 저장하지 않고 해시하여 저장합니다.
4. 인증이 필요한 API는 JWT 기반 인증을 적용합니다.
5. 개인 작업은 별도 브랜치에서 진행한 후 Pull Request로 반영합니다.
