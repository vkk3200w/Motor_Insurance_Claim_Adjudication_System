# Motor Insurance Claim Adjudication System

## 1. Project Overview

The Motor Insurance Claim Adjudication System is a Spring Boot REST API application that automates the evaluation and settlement of motor insurance claims.

The application validates claim requests, checks policy coverage, calculates repair costs and depreciation, evaluates fraud indicators, and determines the final claim status.

It also supports idempotent request processing, claim resubmission, audit tracking, and policy No Claim Bonus (NCB) updates.

## 2. Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.16
- **Build Tool:** Gradle
- **Database:** H2 In-Memory Database
- **Persistence:** Spring Data JPA / Hibernate
- **Validation:** Jakarta Bean Validation
- **API Testing:** cURL / Postman

## 3. Project Architecture

The application follows a layered architecture.

```text
Client Request
      |
      v
Claim Controller
      |
      v
Claim Adjudication Service
      |
      +---- Policy Coverage Rules
      |
      +---- Line Calculation Rules
      |
      +---- Depreciation Rules
      |
      +---- Fraud Detection Rules
      |
      +---- Claim Decision Rules
      |
      +---- Settlement Rules
      |
      v
Repositories (Spring Data JPA)
      |
      v
H2 Database
```

## 4. Main Features

### Claim Processing
- Accepts motor insurance claim requests through REST APIs.
- Validates request fields and required headers.
- Normalizes claimant and vehicle information.
- Calculates vehicle age based on manufacture year and incident date.

### Policy Validation
- Checks policy coverage.
- Identifies third-party-only policies.
- Validates policy dates and active status.

### Claim Calculation
- Calculates gross amount, tax, and line totals.
- Applies part-wise depreciation.
- Handles Zero Depreciation add-ons.
- Handles special flood and theft claim scenarios.
- Calculates deductibles and non-network garage copay.

### Fraud Detection
Supports fraud indicators:
- F01 – Early claim after policy commencement.
- F02 – Theft claim without FIR.
- F03 – Multiple previous claims.
- F04 – Delayed claim submission.
- F05 – Night-time incident.
- F06 – Non-network garage.

### Claim Decision
Determines claim outcomes:
- APPROVED
- REJECTED
- REFERRED
- ZERO_PAYABLE

### Additional Features
- Idempotent request handling using SHA-256 payload hashing.
- Claim resubmission for eligible non-approved claims.
- Audit history for claim submissions and resubmissions.
- NCB reset for eligible claims.
- Transaction management using Spring `@Transactional`.

## 5. REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/claims/adjudicate` | Submit and adjudicate a claim |
| GET | `/api/v1/claims/{claimRef}` | Retrieve claim details |

### Required Headers

| Header | Description |
|---|---|
| X-Correlation-Id | UUID v4 request identifier |
| X-Source-System | PORTAL, AGENT, or PARTNER |
| Content-Type | application/json |

### API Response

The claim adjudication response includes:

- Claim reference
- Correlation ID
- Claim status
- Status reason
- Fraud score
- Total assessed amount
- Total depreciation
- Deductible applied
- Copay amount
- Payable amount
- Fraud flags

## 6. Database Tables

The application uses the following tables:

| Table | Purpose |
|---|---|
| POLICY | Stores insurance policy details |
| CLAIM_HEADER | Stores main claim information |
| CLAIM_LINE | Stores individual claim line items |
| CLAIM_FRAUD_FLAG | Stores triggered fraud rules |
| CLAIM_AUDIT | Maintains claim submission history |
| DEPRECIATION_RULE | Stores depreciation configurations |

## 7. Project Setup

### Prerequisites

- Java 17
- Gradle (or use the included Gradle Wrapper)
- Git

### Clone the Repository

```bash
git clone <your-repository-url>
cd motor-insurance-claim
```

### Build the Project

```bash
./gradlew clean build
```

### Run the Application

```bash
./gradlew bootRun
```

The application runs at:

```text
http://localhost:8080
```

## 8. H2 Database Console

Open:

```text
http://localhost:8080/h2-console
```

Use the configured JDBC URL from `application.properties`.

For the current in-memory setup, the connection URL is:

```text
jdbc:h2:mem:insurance_db
```

Username: `sa`

Password: Leave blank unless configured otherwise.

## 9. API Testing

Example POST request:

```bash
curl -X POST http://localhost:8080/api/v1/claims/adjudicate \
-H "Content-Type: application/json" \
-H "X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000" \
-H "X-Source-System: PORTAL" \
--data-binary @test-data/claim-request.json
```

Example GET request:

```bash
curl http://localhost:8080/api/v1/claims/CLM-2026-000001
```

## 10. Error Handling

The application handles common API errors:

| HTTP Status | Description |
|---|---|
| 201 | New claim successfully processed |
| 200 | Identical request replay |
| 400 | Invalid request or headers |
| 404 | Policy or claim not found |
| 409 | Modification of an approved claim |
| 500 | Unexpected internal server error |

## 11. Testing Scenarios

The project includes test scenarios for:

- New claim submission and retrieval.
- Identical request replay.
- Approved claim modification.
- Non-approved claim resubmission.
- Fraud-based claim referral.
- Constructive Total Loss.
- Flood claim engine-part exclusion.
- Engine protection add-on.
- Zero Depreciation eligibility.
- Zero payable claims.

Additional business-rule and technical tests should be completed before final assessment submission.

## 12. Project Objective

The objective is to provide a structured and maintainable REST-based claim adjudication application using Java and Spring Boot, with configurable business rules, reliable persistence, and automated claim settlement calculations.