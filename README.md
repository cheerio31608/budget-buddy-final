# Budget Buddy

Java 17과 Spring Boot 3 기반으로 구현한 가계부 백엔드 포트폴리오 프로젝트입니다.

단순한 거래 CRUD에 그치지 않고, 금융성 데이터에서 중요한 거래와 잔액의 정합성, 동시성 제어, 중복 요청 방지, 변경 이력 추적을 중심으로 설계했습니다. 또한 Google Gemini API를 연동해 최근 거래 내역을 바탕으로 월별 지출 분석 리포트를 생성합니다.

> 실제 결제 게이트웨이, 카드사 연동, 정산 시스템을 구현한 프로젝트는 아닙니다. 금융 백엔드에서 요구되는 데이터 처리 원칙을 학습하고 검증하는 데 목적이 있습니다.

## 기술 스택

- Java 17
- Spring Boot 3.5
- Spring Web, Spring Data JPA
- Spring Security, Bean Validation
- PostgreSQL, H2
- Flyway
- Google Gemini API
- Gradle
- JUnit 5, Mockito

## 핵심 설계

### 거래와 잔액의 원자성

거래 저장과 사용자 잔액 변경을 하나의 `@Transactional` 경계 안에서 처리합니다. 두 작업 중 하나라도 실패하면 전체 작업을 롤백해 거래와 잔액이 서로 다른 상태로 남지 않도록 했습니다.

### 비관적 락을 이용한 동시성 제어

같은 사용자의 잔액을 변경할 때 `PESSIMISTIC_WRITE` 락을 사용합니다. 동시에 여러 지출 요청이 들어오더라도 최신 잔액을 기준으로 순차 처리해 잔액 부족 거래와 Race Condition을 방지합니다.

### 잔액 스냅샷과 감사 가능성

거래마다 `balanceBefore`, `balanceAfter`를 함께 저장합니다. 거래가 어떤 잔액 상태에서 처리되었고 결과적으로 잔액이 어떻게 변경되었는지 거래 이력만으로 확인할 수 있습니다.

### 멱등성 보장

거래 생성 요청에 `idempotencyKey`를 전달할 수 있습니다. 네트워크 타임아웃이나 사용자의 재시도로 같은 요청이 반복되어도 기존 거래를 반환하고 중복 거래와 중복 차감을 방지합니다.

### 도메인 검증

- 금액은 양수만 허용
- 미래 시점 거래 등록 불가
- 잔액보다 큰 지출 거절
- 카테고리 소유권 검증
- 거래 유형과 카테고리 유형 검증

## 프로젝트 구조

```text
src/main/java/com/finance/budget_buddy
├─ controller       HTTP 요청과 응답 처리
├─ service          거래, 잔액, AI 리포트 비즈니스 로직
├─ repository       Spring Data JPA 데이터 접근
├─ entity           User, Category, Transaction, AiReport
├─ dto              API 요청 및 Gemini 요청/응답 DTO
├─ exception        공통 예외 및 오류 응답
└─ config            Spring Security 설정
```

데이터베이스 스키마는 `src/main/resources/db/migration`의 Flyway 마이그레이션으로 관리합니다.

## 실행 방법

### PostgreSQL 로컬 실행

로컬 PostgreSQL을 사용하는 경우 환경 변수를 설정합니다.

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-local-database-password"
$env:GEMINI_API_KEY="your-gemini-api-key"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="your-admin-password"
```

그 후 애플리케이션을 실행합니다.

```powershell
.\gradlew.bat bootRun
```

`DB_PASSWORD`와 `GEMINI_API_KEY`에는 실제 로컬 환경의 값을 사용해야 합니다. 비밀번호와 API 키는 파일이나 Git 커밋에 저장하지 않습니다.

### PostgreSQL 없이 H2로 실행

API 시나리오 테스트는 인메모리 H2 프로파일로 실행할 수 있습니다.

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=h2 --server.port=18080'
```

H2 프로파일은 Flyway 마이그레이션과 테스트용 시드 데이터를 사용합니다. 애플리케이션을 종료하면 데이터가 초기화됩니다.

## 테스트

```powershell
.\gradlew.bat test
```

테스트는 `test` 프로파일과 H2를 사용하며 로컬 PostgreSQL에 의존하지 않습니다.

주요 검증 범위:

- 거래 생성과 잔액 변경
- 잔액 부족 및 미래 거래 거절
- `balanceBefore`, `balanceAfter` 스냅샷 저장
- `idempotencyKey`를 이용한 중복 요청 방지
- Repository 쿼리
- 비관적 락 기반 동시성 처리

## API

### 거래 생성

```http
POST /api/transactions
Content-Type: application/json
```

```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 1000000,
  "transactionType": "INCOME",
  "description": "월급",
  "transactionAt": "2026-08-25T10:00:00",
  "idempotencyKey": "income-20260825-001"
}
```

### 거래 내역 조회

```http
GET /api/transactions?userId=1
GET /api/transactions/{transactionId}
```

### 월별 AI 리포트 생성 및 조회

```http
GET /api/ai/report/monthly?userId=1
GET /api/ai/reports?userId=1
```

AI 리포트 생성 API는 최근 30일 거래 내역을 Gemini API에 전달하고, 생성 결과를 `ai_reports` 테이블에 저장합니다. 이 기능을 실제로 사용하려면 `GEMINI_API_KEY`가 필요합니다.

### 테스트 데이터 생성

`local` 프로파일에서만 제공되며 Basic Auth가 필요합니다.

```http
POST /api/test/data/generate?userId=1&count=30
Authorization: Basic <관리자 계정>
```

## API 시나리오 테스트 결과

H2 환경에서 다음 시나리오를 검증했습니다.

| 검증 항목 | 결과 |
|---|---|
| 초기 거래 조회 | `200 OK`, 거래 0건 |
| 수입 1,000,000원 등록 | 잔액 `0 -> 1,000,000` |
| 지출 250,000원 등록 | 잔액 `1,000,000 -> 750,000` |
| 동일 멱등성 키 재요청 | 기존 거래 반환, 중복 row 없음 |
| 잔액 부족 지출 | `400`, `T002` |
| 미래 날짜 거래 | `400`, `C001` |
| 잘못된 금액 | `400`, `C001` |
| 최종 거래 조회 | 정상 거래 2건 |

총 8개 요청을 실행했으며, 성공 5건과 도메인 규칙에 따른 거절 3건을 확인했습니다.

## 문서 및 시각화

- [Notion 페이지](https://app.notion.com/p/Budget-Buddy-3ca6f800cbc2805b828af7895705219b?source=copy_link)

## 관리자 데모

```text
http://localhost:8080/admin
```

관리자 계정은 `ADMIN_USERNAME`, `ADMIN_PASSWORD` 환경 변수로 설정합니다. 기본값은 로컬 데모 편의를 위한 값이며 운영 환경에서 그대로 사용하지 않습니다.

## 향후 개선 방향

- 대량 거래를 고려한 통계 DTO 및 집계 쿼리 도입
- AI 리포트 비동기 처리와 월별 결과 캐싱
- 외부 Gemini API 실패·지연에 대한 재시도 및 타임아웃 정책
- 실제 PostgreSQL 환경에서의 동시성 통합 테스트 보강
- 사용자 인증 및 권한 모델 고도화

