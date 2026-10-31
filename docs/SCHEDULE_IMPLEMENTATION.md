# 일정 생성·저장 구현 및 검증

목표 생성 시 일정이 저장되도록 연결하고, 오늘 일정 조회·완료·목표 진행률·기록 집계를 실제 DB 기반으로 처리한다. 10쪽을 3일에 배분하면 4·3·3이며, 완료 날짜를 제외한 남은 분량만 재배분한다.

## 실행과 설정

기존 application.properties의 MySQL·8081 설정을 유지한다. 별도 테스트 DB에서 먼저 검증한다. `database/schedule_record_addition.sql`은 USER·GOAL이 준비된 DB에 일정·기록 테이블을 추가하는 용도이며 기존 테이블을 변경하거나 삭제하지 않는다. 기존 테이블 구조가 다르면 이 스크립트가 자동으로 수정하지 않으므로 Entity와 맞춰야 한다. 실제 MySQL 검증은 아직 완료되지 않았다.

MySQL 없이 데모를 실행하려면 backend 폴더에서 다음과 같이 실행한다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

데모는 H2 메모리 DB·8082를 사용한다. 종료하면 데모 데이터가 사라진다. Swagger는 http://localhost:8082/swagger-ui/index.html 이며 명세 주소는 /v3/api-docs다.

## 직접 확인

1. POST /api/auth/signup으로 사용자를 만들고 응답 userId를 확인한다.
2. 이후 목표·일정·기록 요청에는 X-User-Id 헤더를 명시한다. 기본 사용자 1로 처리하던 동작을 제거했으므로 헤더가 없으면 400이다.
3. 오늘부터 3일 동안 10쪽 목표를 만들고 GET /api/schedules/today로 오늘 4쪽을 확인한다.
4. PATCH /api/schedules/{scheduleId}/complete를 본문 없이 호출한다. 완료량 4, 진행률 40이 된다.
5. 중복 완료는 409이며 기록은 추가되지 않는다. 오류 형식은 status·error·message이고 error는 CONFLICT다.
6. 종료일 연장 시 완료 일정·기록을 보존하고 미완료 일정만 교체한다.

## API와 정책

기본 API 경로와 성공 응답 필드를 유지한다. 다만 위 사용자 헤더 필수화와 중복 완료 409는 프론트 담당자가 반영해야 하는 동작이다. X-User-Id는 개발용 식별 수단이며 실제 인증이 아니다.

- POST /api/schedules/preview/{goalId}: 저장하지 않는 계산 미리보기. 실제 완료 분량·완료 날짜를 제외하며 휴무일·하루 한도는 미리보기에서만 지원한다.
- POST /api/schedules/replan/{goalId}: 지난 미완료 분량을 오늘부터 재배분하는 추가 경로. 자동 야간 배치는 없다. 조회 GET은 저장 데이터를 변경하지 않는다.
- 미래 일정 완료는 409, 다른 사용자 자원 접근은 404다.
- 공부 시간 요청 필드는 원본 명세에 없으므로 study_time은 null, 시간 집계는 0으로 처리한다.
- 재계산은 완료 행과 RECORD를 보존한다. 과거 미완료 행을 교체하므로 과거 완료율도 달라질 수 있다. 계획 이력 저장은 별도 기능이다.
- 모든 일정 완료 시 목표 상태를 COMPLETED로 변경한다. 총분량이 완료량보다 작거나 완료 날짜를 제외하는 기간 변경은 거절하고 롤백한다.
- 목표 삭제 시 기록→일정→목표 순서로 정리한다.

## 검증

```powershell
cd backend
.\gradlew.bat test bootJar
```

PlanningIntegrationTest 18개와 PlanningApiTest 6개로 구성된다. 테스트는 demo 프로필의 격리 H2와 고정 날짜를 사용한다. DB 저장·합계 보존·완료·중복·동시 완료·재계산·롤백·삭제 및 Spring MockMvc 요청/응답 흐름을 검증한다. MySQL 실서버, 별도 HTTP 클라이언트, 프론트 화면, 부하 테스트 결과를 의미하지 않는다.

JWT·캐릭터 경험치·성장 이력·AWS는 기존 코드 상태이며 이번 변경에 구현하지 않았다.
