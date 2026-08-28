# Budget Buddy

Budget Buddy is a Java 17 and Spring Boot 3 backend portfolio project for household finance data processing.

This project is not presented as a real payment gateway. It focuses on backend capabilities that are useful in financial-style systems: transaction consistency, balance snapshots, validation, exception handling, persistence design, tests, and external AI API integration.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- H2 for tests
- Flyway
- Google Gemini API
- Gradle
- JUnit 5, Mockito
- springdoc-openapi Swagger UI

## Core Features

- Create income and expense transactions.
- Update user balance in the same transaction.
- Store `balanceBefore` and `balanceAfter` snapshots.
- Reject future transaction dates.
- Reject expenses when balance is insufficient.
- Validate request bodies with Bean Validation.
- Model transaction type with `TransactionType`.
- Verify category ownership before transaction creation.
- Serialize balance updates with pessimistic locking.
- Prevent duplicate requests with optional `idempotencyKey`.
- Generate Gemini-based monthly reports and persist them in `ai_reports`.

## Run

```bash
./gradlew bootRun
```

On Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

For API scenario testing without a local PostgreSQL database, run the in-memory H2 profile:

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=h2 --server.port=18080'
```

The local profile uses PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/budget_buddy_portfolio
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
```

Set environment variables when needed:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-local-database-password"
$env:GEMINI_API_KEY="your-api-key"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="admin1234"
```

`DB_PASSWORD` has no committed fallback value. This prevents the application from accidentally trying a demo password such as `1234` against your real local PostgreSQL account.

If PostgreSQL authentication still fails after changing `DB_PASSWORD`, stop old Gradle daemons so they do not reuse stale environment variables:

```powershell
.\gradlew.bat --stop
```

## Test

From the repository root:

```bash
./gradlew test
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
```

Tests run with the `test` profile and an in-memory H2 database. Flyway creates the schema, so tests do not depend on a local PostgreSQL instance.

## API Docs

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Main APIs

### Create Transaction

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
  "description": "Initial charge",
  "transactionAt": "2026-08-25T10:00:00",
  "idempotencyKey": "charge-20260825-001"
}
```

### List Transactions

```http
GET /api/transactions?userId=1
```

### Get Transaction

```http
GET /api/transactions/{transactionId}
```

### Generate Monthly AI Report

```http
GET /api/ai/report/monthly?userId=1
```

### List AI Reports

```http
GET /api/ai/reports?userId=1
```

### Generate Sample Data

This endpoint is enabled only in the `local` profile and requires Basic Auth.

```http
POST /api/test/data/generate?userId=1&count=30
Authorization: Basic admin/admin1234
```

### Local Test APIs

These endpoints are enabled only in the `local` profile.

```http
GET /api/test/db
GET /api/test/expense/{userId}
```

## Admin Demo

```text
http://localhost:8080/admin
```

Basic Auth defaults:

- username: `admin`
- password: `admin1234`

The credentials are demo defaults and can be changed through `ADMIN_USERNAME` and `ADMIN_PASSWORD`.

## Portfolio Message

Budget Buddy demonstrates financial-style backend data handling:

- atomic transaction and balance updates
- balance history snapshots
- insufficient balance prevention
- category ownership validation
- API request validation
- database constraints and indexes through Flyway
- concurrency-aware balance updates using pessimistic locking
- repeat request protection through idempotency keys
- service unit tests and repository/integration tests

It does not include PG/VAN/card issuer integration, settlement, or production-grade authentication.
