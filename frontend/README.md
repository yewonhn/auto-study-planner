# Frontend

FocusGrow의 사용자 화면 및 클라이언트 기능을 담당합니다.

## 담당 기능

- 회원가입 / 로그인 화면
- 목표 설정 화면
- 오늘의 일정 및 학습 계획 화면
- 일정 완료 처리
- 학습 기록 조회
- MY 페이지
- 캐릭터 성장 정보 표시
- Backend API 연동

## 개발 시 참고

API 명세는 `docs/API_DB_SPEC.md`를 참고합니다.

주요 API 영역:

- `/api/auth` : 회원가입 / 로그인
- `/api/users` : 사용자 정보
- `/api/goals` : 목표 관리
- `/api/schedules` : 일일 일정 관리
- `/api/records` : 학습 기록
- `/api/character` : 캐릭터 정보

## 작업 규칙

1. 화면별 기능을 작업하기 전에 API 명세를 확인합니다.
2. API 변경이 필요한 경우 Backend 담당자와 협의합니다.
3. 공통 UI 및 컴포넌트는 재사용을 우선합니다.
4. 개인 작업은 별도 브랜치에서 진행한 후 Pull Request로 반영합니다.
