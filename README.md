# Motor Insurance Claim Adjudication System

A rule-based Motor Insurance Claim Adjudication REST API developed using Java and Spring Boot. The application automates the evaluation of motor insurance claims by validating policy coverage, calculating repair costs and depreciation, identifying potential fraud, and determining the final claim settlement amount.

---

## 1. Project Overview

The Motor Insurance Claim Adjudication System is designed to simplify and automate the motor insurance claim processing workflow.

When a claim is submitted, the application performs multiple validation and calculation steps before deciding whether the claim should be approved, rejected, or referred for further investigation.

The system evaluates:
- Insurance policy validity and coverage.
- Claimant, vehicle and incident information.
- Repair line items, taxes and depreciation.
- Special conditions such as theft, flood and Zero Depreciation add-ons.
- Fraud indicators and risk scores.
- Deductibles, garage copay and final settlement amounts.

The application also supports idempotent request handling, claim resubmission, audit history and policy No Claim Bonus (NCB) updates.

## 2. Project Specifications

| Specification | Details |
|---|---|
| Project Name | Motor Insurance Claim Adjudication System |
| Application Type | RESTful Backend Application |
| Programming Language | Java 17 |
| Java Runtime | OpenJDK 17.0.12 |
| Framework | Spring Boot 3.5.16 |
| Build Tool | Gradle (Gradle Wrapper) |
| Database | H2 In-Memory Database |
| ORM | Hibernate / Spring Data JPA |
| Validation | Jakarta Bean Validation |
| API Format | JSON |
| API Testing | cURL / Postman |
| Database Console | H2 Console |
| Default Server Port | 8080 |

## 3. Technology Stack

**Java 17**

Used to implement the application logic, object-oriented design, business rules and data processing.

**Spring Boot 3.5.16**

Provides the application framework for developing REST APIs, dependency injection, configuration, exception handling and transaction management.

**Spring Data JPA**

Handles database operations through repositories and simplifies persistence and retrieval of claim, policy and audit information.

**Hibernate**

Acts as the JPA implementation responsible for mapping Java entities to database tables.

**H2 Database**

An in-memory relational database used for development, sample data initialization and testing claim processing scenarios.

**Gradle**

Manages project dependencies, compilation, testing and application builds through the Gradle Wrapper.

**Jakarta Bean Validation**

Validates incoming request fields, required values, formats and business-related input constraints.

## 4. Application Architecture

The application follows a layered architecture to maintain separation of concerns and make the code easier to maintain.

```text
                 CLIENT
                   |
                   v
            CLAIM CONTROLLER
                   |
                   v
       CLAIM ADJUDICATION SERVICE
                   |
          +--------+--------+
          |        |        |
          v        v        v
       POLICY   CALCULATION FRAUD
       RULES      RULES     RULES
          |        |        |
          +--------+--------+
                   |
                   v
            DECISION ENGINE
                   |
                   v
          SETTLEMENT & NCB
                   |
                   v
             REPOSITORIES
                   |
                   v
              H2 DATABASE
```

### Main Application Layers

| Layer | Responsibility |
|---|---|
| Controller | Accepts HTTP requests and returns API responses |
| DTO | Defines request and response data structures |
| Service | Coordinates the complete claim adjudication workflow |
| Rules | Contains individual policy, calculation, fraud and decision rules |
| Repository | Performs database operations |
| Entity | Represents database tables |
| Exception | Handles application and API errors |
| Validation | Validates request data and headers |

## 5. Claim Adjudication Workflow

The claim processing workflow follows these steps:

1. Receive the claim request through the REST API.
2. Validate mandatory headers and request fields.
3. Normalize claimant and vehicle information.
4. Retrieve the corresponding insurance policy.
5. Verify policy coverage and validity.
6. Calculate repair line items, taxes and depreciation.
7. Apply special rules for theft, flood and add-ons.
8. Evaluate fraud indicators and calculate the fraud score.
9. Determine the claim decision based on business rules.
10. Calculate the final settlement amount.
11. Update the policy NCB when applicable.
12. Save claim details, line items, fraud flags and audit history.
13. Return the final adjudication response.

## 6. Business Rules Implemented

The application implements the following business rule groups:

| Rule | Description |
|---|---|
| R1 | Request normalization, claimant information and vehicle age calculation |
| R2 | Policy coverage validation, policy dates and policy status |
| R3 | Gross amount, tax calculation and line-item totals |
| R4 | Part-wise depreciation and Zero Depreciation add-on handling |
| R5 | Special handling for flood and theft claims |
| R6 | Claim settlement, deductible, copay and Constructive Total Loss (CTL) |
| R7 | Fraud detection and risk scoring |
| R8 | Final claim decision and status determination |
| R9 | Policy No Claim Bonus (NCB) update |

### Claim Decision Outcomes

The system supports four primary claim outcomes:

- **APPROVED:** Claim satisfies the approval conditions.
- **REJECTED:** Claim fails mandatory policy or coverage conditions.
- **REFERRED:** Claim requires additional investigation due to fraud risk, theft, CTL or high claim value.
- **ZERO_PAYABLE:** Claim has no payable settlement amount.

### Fraud Detection Rules

| Rule Code | Description | Points |
|---|---|---:|
| F01 | Claim incident within 30 days of policy commencement | 30 |
| F02 | Theft claim without FIR | 40 |
| F03 | Two or more previous claims | 25 |
| F04 | Claim submitted more than 30 days after incident | 15 |
| F05 | Incident occurring between 23:00 and 04:59 | 10 |
| F06 | Non-network garage | 10 |

## 7. REST API Endpoints

The application exposes the following endpoints:

| HTTP Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/claims/adjudicate` | Submit and adjudicate a motor insurance claim |
| GET | `/api/v1/claims/{claimRef}` | Retrieve an existing claim using its reference |

### Required Request Headers

| Header | Description | Accepted Values |
|---|---|---|
| X-Correlation-Id | Unique request tracking identifier | UUID v4 |
| X-Source-System | Identifies the claim source | PORTAL, AGENT, PARTNER |
| Content-Type | Request content format | application/json |

### Example POST Request

```bash
curl -X POST http://localhost:8080/api/v1/claims/adjudicate \
-H "Content-Type: application/json" \
-H "X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000" \
-H "X-Source-System: PORTAL" \
--data-binary @test-data/claim-request.json
```

### Example GET Request

```bash
curl http://localhost:8080/api/v1/claims/CLM-2026-000001
```

### Response Information

The API response contains:

- Claim reference and correlation ID.
- Claim status and status reason.
- Fraud score and triggered fraud flags.
- Total assessed amount.
- Total depreciation.
- Deductible applied.
- Garage copay amount.
- Final payable amount.

## 8. Database Design

The application uses an H2 relational database with the following main entities:

| Table | Description |
|---|---|
| POLICY | Stores insurance policy details, coverage dates, IDV, deductible and NCB |
| CLAIM_HEADER | Stores the main claim information and final adjudication result |
| CLAIM_LINE | Stores individual repair items, taxes, depreciation and approved amounts |
| CLAIM_FRAUD_FLAG | Stores fraud rules triggered during claim evaluation |
| CLAIM_AUDIT | Maintains claim submission and resubmission history |
| DEPRECIATION_RULE | Stores part-category and vehicle-age-based depreciation percentages |

### Database Relationships

- One policy can be associated with multiple claims.
- One claim header can contain multiple claim line items.
- One claim header can have multiple fraud flags.
- One claim header can have multiple audit records.

## 9. Additional Technical Features

### Idempotency

The application uses SHA-256 hashing of the raw request payload to identify identical claim submissions.

- Identical request: returns HTTP 200.
- New claim: returns HTTP 201.
- Changed payload for an approved claim: returns HTTP 409.
- Eligible non-approved claim modification: processes as a resubmission.

### Claim Resubmission

Non-approved claims can be resubmitted with modified information. Existing claim line items and fraud flags are replaced while preserving the claim header ID and audit history.

### Transaction Management

Spring's `@Transactional` annotation is used to manage database operations and maintain consistency during claim processing.

### Optimistic Locking

The policy entity uses a version field with JPA optimistic locking to help prevent conflicting concurrent updates.

### Exception Handling

Centralized exception handling provides consistent HTTP error responses for invalid requests, missing records, conflicts and unexpected application errors.

## 10. Project Directory Structure

```text
motor-insurance-claim/
│
├── src/
│   ├── main/
│   │   ├── java/com/company/insurance/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── rules/
│   │   │   ├── service/
│   │   │   └── validation/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   │
│   └── test/
│
├── test-data/
│   ├── claim-request.json
│   ├── claim-request-modified.json
│   ├── zero-payable-claim.json
│   ├── referred-theft-claim.json
│   ├── ctl-claim.json
│   ├── flood-claim.json
│   ├── zero-dep-eligible.json
│   └── zero-dep-ineligible.json
│
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

## 11. Prerequisites

Ensure the following are installed:

- Java Development Kit (JDK) 17
- Git
- Visual Studio Code or another Java IDE
- Terminal / Command Line

Gradle can be executed using the included Gradle Wrapper, so a separate Gradle installation is not required.

## 12. Installation and Execution

### Step 1: Clone the repository

```bash
git clone https://github.com/vkk3200w/Motor_Insurance_Claim_Adjudication_System.git
```

### Step 2: Navigate to the project

```bash
cd Motor_Insurance_Claim_Adjudication_System
```

### Step 3: Verify Java version

```bash
java -version
```

Ensure Java 17 is being used.

### Step 4: Build the application

```bash
./gradlew clean build
```

### Step 5: Run the application

```bash
./gradlew bootRun
```

The application will start on:

```text
http://localhost:8080
```

## 13. H2 Database Console

The H2 Console can be accessed at:

```text
http://localhost:8080/h2-console
```

Use the following connection details for the current in-memory database configuration:

| Property | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:insurance_db` |
| Username | `sa` |
| Password | Blank, unless configured otherwise |
| Database Type | H2 |
| Persistence | In-memory |

The database is initialized with sample policy and depreciation data. Since it is an in-memory database, data is cleared when the application restarts.

## 14. Testing Scenarios

The following scenarios have been exercised during API testing:

| Test Scenario | Verified Result |
|---|---|
| New claim submission | 201 Created |
| Claim retrieval | 200 OK |
| Identical request replay | 200 OK |
| Modified approved claim | 409 Conflict |
| Missing claim reference | 404 Not Found |
| Non-approved claim resubmission | 201 Created |
| Zero payable claim | ZERO_PAYABLE |
| Theft claim without FIR and multiple prior claims | REFERRED |
| Constructive Total Loss | REFERRED |
| Flood without ENGINE_PROTECT | Engine part excluded |
| Flood with ENGINE_PROTECT | Engine part included |
| ZERO_DEP eligible vehicle | 0% depreciation |
| ZERO_DEP ineligible vehicle | Normal depreciation applied |

Further verification of all remaining business rules, validation cases, transaction rollback and concurrency handling is part of final testing.

## 15. HTTP Status Codes

| Status Code | Meaning |
|---|---|
| 200 | Successful retrieval or identical request replay |
| 201 | New claim or eligible resubmission processed |
| 400 | Invalid request or missing/invalid headers |
| 404 | Claim or policy not found |
| 409 | Approved claim modification conflict |
| 500 | Unexpected internal server error |

## 16. Future Enhancements

Potential improvements include:

- Integration with a persistent database such as PostgreSQL.
- API documentation using Swagger / OpenAPI.
- Automated unit and integration test coverage.
- Authentication and role-based authorization.
- Centralized logging and monitoring.
- Docker-based deployment.
- CI/CD pipeline integration.

## 17. Project Objective

This project demonstrates the development of a structured backend application using Java and Spring Boot, with an emphasis on business-rule implementation, REST API development, database persistence, transaction management, exception handling and automated claim adjudication.

The layered architecture and modular rule-engine approach make the application easier to maintain, test and extend.