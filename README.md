# API Testing Framework

**Version:** 1.0  
**Author:**Team  
**Language:** Java  
**Build Tool:** Maven  

---

## 1. Overview

The **API Testing Framework** is an enterprise-grade automation solution designed to validate RESTful APIs with high reliability and flexibility.  
It integrates **Cucumber (BDD)**, **Rest-Assured**, **WireMock**, and **Allure Reporting** to support comprehensive, automated API validation for middleware and backend services.

This framework provides a modular foundation for:
- Automated functional and regression API testing  
- Scenario-based validation using BDD  
- Mocking and stubbing of API endpoints  
- Data-driven test execution with centralized reporting  

---

## 2. Key Features

- **BDD Implementation:** Behavior-driven testing with Cucumber feature files  
- **Mocking Support:** WireMock-based API virtualization for controlled testing  
- **Reusable Components:** Modular core classes for configuration, logging, and data handling  
- **Data-Driven Testing:** Support for CSV-based input and output  
- **Flexible Configuration:** Environment-specific setup via XML and JavaScript configuration files  
- **Allure Reporting:** Interactive and visual reports for test analysis  
- **Logging & Traceability:** Log4j2-based structured execution logs  

---

## 3. Architecture & Components

### 3.1 Core Modules
| Module | Description |
|--------|--------------|
| **core** | Base classes for API testing, request specs, and response handling |
| **config** | Configuration management |
| **dsl** | Domain-specific utilities for BDD step creation |
| **models** | POJOs for request and response structures |
| **utilities** | CSV utilities and shared helper methods |

### 3.2 Test Modules
| Module | Description |
|--------|--------------|
| **runners** | Cucumber test runners |
| **hooks** | Setup and teardown logic |
| **steps** | Step definition classes |
| **features** | BDD feature files |
| **stubs** | WireMock stubs for simulated responses |

---

## 4. Project Structure

```
ApiTesting/
├── pom.xml
├── src/
│   ├── main/java/com/apitesting/
│   │   ├── core/
│   │   ├── config/
│   │   ├── dsl/
│   │   ├── models/
│   │   └── utilities/
│   ├── main/resources/
│   │   └── log4j2.xml
│   ├── test/java/com/apitesting/
│   │   ├── hooks/
│   │   ├── runners/
│   │   └── steps/
│   └── test/resources/
│       ├── features/
│       ├── stubs/
│       ├── config.js
│       └── log4j.properties
└── .gitignore
```

---

## 5. Prerequisites

| Requirement | Version |
|--------------|----------|
| Java | 11 or higher |
| Maven | 3.6+ |
| IntelliJ IDEA (Recommended) | Latest version with Cucumber plugin |
| Allure Commandline | 2.20+ |

> 🛈 Install Allure using:
> ```bash
> scoop install allure    # (Windows)
> brew install allure     # (macOS)
> ```

---

## 6. Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/ApiTesting.git
   cd ApiTesting
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run sample tests to verify setup:
   ```bash
   mvn test
   ```

---

## 7. Test Execution

### 7.1 Run All Tests
```bash
mvn test
```

### 7.2 Run a Specific Feature
```bash
mvn test -Dcucumber.options="src/test/resources/features/tokens/tora.feature"
```

### 7.3 Run from IDE
Open `TestRunner.java` and execute it as a **JUnit Test**.

---

## 8. Allure Reporting

### 8.1 Report Generation
After test execution, Allure results are automatically stored in:
```
target/allure-results/
```

Generate and open the report using:
```bash
allure serve target/allure-results
```

Or build the static HTML version:
```bash
allure generate target/allure-results --clean -o target/allure-report
```

### 8.2 Report Features
- Detailed step-by-step execution logs  
- Cucumber scenario hierarchy  
- Attachments for request/response payloads  
- Trend analysis (if integrated with CI/CD)  
- Status summary (Passed / Failed / Broken / Skipped)

---

## 9. Configuration

Configuration files:
- `config.js` – Environment and endpoint configuration  
- `log4j2.xml` – Logging configuration  
- `pom.xml` – Build and plugin management (including Allure)  

---

## 10. Mocking and Virtualization

WireMock is used to simulate API responses for isolated testing.  

Example stub file (`src/test/resources/stubs/greet-mapping.json`):
```json
{
  "request": { "method": "GET", "url": "/greet" },
  "response": { "status": 200, "body": "Hello, User!" }
}
```

To enable WireMock, initialize via `WireMockFactory.java`.

---

## 11. Test Data Management

Utilities:
- `CSVReadUtil` – Read test data from CSV  
- `CSVWriteUtil` – Export results or audit logs  

Test data files should be placed under:
```
src/test/resources/data/
```

---

## 12. Logging

Logging via **Log4j2** ensures traceability:
- Config: `src/main/resources/log4j2.xml`
- Log levels: INFO, DEBUG, ERROR
- Output: Console + log files (if configured)

---

## 13. CI/CD Integration

In a CI environment (e.g., Jenkins, GitHub Actions, Azure DevOps):

```bash
mvn clean test
allure generate target/allure-results --clean -o target/allure-report
allure open target/allure-report
```

Integrate Allure publishing as a post-build step for visibility in your pipeline dashboard.

---

## 14. Maintenance Guidelines

- Maintain consistent naming conventions for feature and step files.  
- Store reusable components in `/core` and `/utilities`.  
- Keep environment-specific properties externalized.  
- Validate API tests against both live and mocked data.  

---

## 15. Contact & Support

For framework-related queries or requests:
- **Email:**support@yourcompany.com  
- **Team:** QA Automation / Middleware Services  
- **Location:** Athens HQ  

---

© 2025 Automation Framework. All rights reserved.
