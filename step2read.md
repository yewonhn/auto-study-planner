# FocusGrow (auto-study-planner)

목표와 기간을 입력하면 공부할 분량을 일별로 자동으로 쪼개 주는 ADHD 스터디 플래너 앱입니다.

## 1. 저장소 구조

```text
auto-study-planner/
├── README.md      ← 지금 보는 문서
├── backend/       Spring Boot API 서버
├── frontend/      화면
├── database/      DB 관련 파일
└── docs/          API/DB 명세서, ERD
```

각자 **자기 담당 폴더만** 수정합니다. 다른 폴더를 고쳐야 하면 담당자에게 먼저 알려 주세요.

## 2. 현재 진행 상태

API 14개의 **주소와 요청/응답 JSON 구조는 모두 열려 있습니다.** 다만 실제 기능이 구현된 것과 임시 응답을 주는 것이 섞여 있습니다.

| 구분 | API | 상태 |
|---|---|---|
| 실제 동작 (DB 연동) | `POST /api/auth/signup`, `POST /api/auth/login`, `GET /api/users/me` | 동작. 단, 로그인의 `accessToken`은 임시 문자열(`TEMP_ACCESS_TOKEN`) |
| 실제 동작 (DB 연동) | `POST/GET /api/goals`, `GET/PATCH/DELETE /api/goals/{goalId}` | 동작. 단, `progress`, `completedAmount`는 임시값 `0` |
| 임시 응답 (고정값) | `GET /api/schedules/today`, `PATCH /api/schedules/{scheduleId}/complete` | 항상 같은 값을 반환하고 저장하지 않음 |
| 임시 응답 (고정값) | `GET /api/records`, `GET /api/records/monthly` | 위와 동일 |
| 임시 응답 (고정값) | `GET /api/character`, `GET /api/character/history` | 위와 동일 |

임시 응답 API는 코드에 `TODO` 주석이 있습니다. 실제 로직이 구현되면 응답 값만 바뀌고 JSON 구조는 유지됩니다.

## 3. GitHub 작업 방법 (모두 공통)

최소한의 순서만 지키면 됩니다.

```bash
# 처음 한 번: 내려받기
git clone https://github.com/yewonhn/auto-study-planner.git
cd auto-study-planner

# 작업 시작 전: 항상 최신으로
git checkout main
git pull

# 내 작업용 브랜치 만들기 (main에서 직접 작업하지 않기)
git checkout -b frontend/홈화면          # 예: frontend/..., algorithm/...

# 수정 후: 올라갈 파일 확인하고 저장
git add .
git status                               # 의도하지 않은 파일이 없는지 꼭 확인
git commit -m "무엇을 했는지 한 줄"

# 올리기
git push -u origin frontend/홈화면
```

그다음 GitHub 저장소 페이지에서 **Compare & pull request → Create pull request → Merge pull request**로 `main`에 합칩니다. 합쳐진 뒤에는 모두 `git pull`로 받습니다.

주의할 점은 다음과 같습니다.

- **push 권한이 없으면** 저장소 소유자(한예원)에게 Collaborator 초대를 요청하세요. (Settings → Collaborators)
- 같은 파일을 두 사람이 동시에 고치면 충돌이 납니다. 자기 폴더만 수정하면 대부분 피할 수 있습니다.
- **비밀번호, 토큰 등 비밀정보는 절대 커밋하지 않습니다.** (아래 4번 참고)
- `git status`에 `build/`, `.gradle/`, `.idea/` 같은 폴더가 보이면 올리지 말고 질문해 주세요.

## 4. 백엔드 서버 실행

프론트엔드, 알고리즘 담당 모두 API를 직접 호출해 보려면 서버를 내 PC에서 실행해야 합니다. (AWS 배포 전)

필요한 것은 JDK 21, MySQL 8.0, IntelliJ IDEA입니다.

1. **MySQL에 DB 생성**

   ```sql
   CREATE DATABASE focusgrow CHARACTER SET utf8mb4;
   ```

2. **비밀번호 파일 만들기**
   `backend/src/main/resources/local-secret.properties.example`을 같은 폴더에 복사해서 `local-secret.properties`로 이름을 바꾸고, 본인 MySQL 비밀번호를 넣습니다. 이 파일은 `.gitignore`에 들어 있어 Git에 올라가지 않습니다.

3. **IntelliJ에서 열기**
   `auto-study-planner` 전체가 아니라 **`backend` 폴더**를 엽니다. Gradle 로딩이 끝날 때까지 기다린 뒤 `FocusGrowApplication.java`의 `main`을 실행합니다. 콘솔에 `Started FocusGrowApplication`이 나오면 성공입니다.

   기존 Gradle 실행 설정에서 오류가 나면 **Application** 실행 설정을 새로 만들어 쓰세요.

4. **확인**
   - 서버 주소: `http://localhost:8081`
   - Swagger(API 목록, 직접 호출 가능): `http://localhost:8081/swagger-ui/index.html`

5. **테스트 사용자 만들기**
   Swagger에서 `POST /api/auth/signup`을 실행해 사용자를 한 명 만듭니다. 빈 DB에서 **처음 가입한 사용자의 `userId`는 1**입니다.

## 5. 프론트엔드 담당 가이드

수정은 **`frontend` 폴더만** 합니다. 서버 코드는 건드리지 않아도 됩니다.

- **Base URL**은 `http://localhost:8081`입니다. (명세서에는 8080으로 적혀 있지만 실제 서버는 8081입니다.)
- **인증**: JWT는 아직 없습니다. 인증이 필요한 API는 임시로 요청 헤더 `X-User-Id: 1`을 쓰고, 헤더를 생략하면 1번 사용자로 처리됩니다. JWT가 적용되면 `Authorization: Bearer {accessToken}`으로 바뀔 예정입니다.
- **요청/응답 형식**은 `http://localhost:8081/swagger-ui/index.html`의 예시를 기준으로 합니다. 날짜는 `yyyy-MM-dd`입니다.
- **에러 응답**은 모두 같은 형식입니다.

  ```json
  { "status": 404, "error": "NOT_FOUND", "message": "목표를 찾을 수 없습니다." }
  ```

  상태 코드는 `400`(요청 오류), `401`(로그인 실패), `404`(대상 없음), `500`(서버 오류)을 씁니다.
- **CORS**: 웹 브라우저에서의 호출은 개발 중 모든 출처에 허용되어 있습니다. (배포 전에 프론트 주소로 제한할 예정)
- **임시 응답 주의**: 위 2번 표의 임시 응답 API는 누가 호출해도 같은 값을 줍니다. "데이터가 안 바뀐다"면 버그가 아니라 아직 구현 전이기 때문입니다.
- `backend/script.js`는 프론트 파일로 보입니다. 담당자가 확인해서 `frontend` 폴더로 옮겨 주세요. 이 파일의 `BASE_URL`이 `8080`이면 `8081`로 바꿔야 연결됩니다.

## 6. 알고리즘 담당 가이드 (일정 자동 분배 · 재계산)

수정은 **`backend/src/main/java/com/focusgrow/backend/schedule/`** 패키지 중심으로 합니다.

- **입력**: 목표(`Goal`: 시작일, 종료일, 전체 분량, 단위). 코드는 `goal/Goal.java`에 있습니다.
- **출력**: 날짜별 일정(`SCHEDULE`: 날짜, 제목, 분량 등). 컬럼 구조는 `docs/DB_API_SPEC.md`의 3.3 SCHEDULE을 따릅니다.
- **현재 상태**
  - `schedule/ScheduleController.java`는 임시 응답만 줍니다. 실제 조회/완료 처리로 교체하면 됩니다.
  - 일정(`SCHEDULE`) 엔티티와 테이블은 아직 없습니다.
  - 일일 분량(`dailyAmount`)은 임시로 `goal/dto`의 `GoalResponse`, `GoalDetailResponse` 안에서 계산합니다. 시작일~종료일을 **양 끝 포함** 일수로 나누고 **올림**한 값입니다. 알고리즘이 정해지면 그쪽으로 옮겨 주세요.
- **작업 방식**: 알고리즘은 `schedule` 패키지에 **새 파일**(예: `ScheduleGenerator`)로 추가하고, 기존 파일은 필요한 부분만 최소로 고칩니다.
- **응답 JSON 형식을 바꿔야 할 때**는 코드를 고치기 전에 백엔드 담당(Backend1)과 먼저 합의하세요. 프론트가 이 형식을 보고 개발 중이라, 필드를 **추가**하는 것은 안전하지만 이름을 바꾸거나 **삭제**하면 프론트가 깨질 수 있습니다.

## 7. 팀 협의가 필요한 사항 (미정)

- 로그인 ID: 현재는 **이메일**로 가입/로그인합니다. 제안서의 "아이디 또는 이메일"과 다르므로 방식을 정해야 합니다.
- 공부 가능 일수: 제안서의 목표설정에는 있지만 현재 목표 생성 요청에는 필드가 없습니다.
- 공부 시간 저장: 일정 완료 요청이 본문 없이 호출되어 공부 시간을 받을 수 없습니다.
- 일수 계산 기준: 제안서는 "60일", 현재 구현은 양 끝 포함 61일(2026-10-01~2026-11-30 기준)입니다.
- 미달성 일정 재계산을 별도 API로 둘지, 서버 내부 처리로 둘지 정해야 합니다.
- 회원 정보 수정/탈퇴 API는 아직 없습니다.

## 8. 문서

- `docs/DB_API_SPEC.md`: DB 설계와 REST API 명세서입니다. 일부 값(포트 8081, 임시 응답 표시 등)은 실제 구현과 다르며 수정 예정입니다. 차이가 있으면 **Swagger와 이 README를 기준**으로 봐 주세요.
- API 요청/응답 형식을 바꾸면 팀에 공유하고 명세서도 함께 수정합니다.
