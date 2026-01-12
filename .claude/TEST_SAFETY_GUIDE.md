# Test Safety Guide - SayMyName Backend

**CRITICAL**: This document ensures tests **NEVER** connect to production/dev MySQL databases.

---

## 🚨 Safety Mechanisms

### 1. Fail-Fast Guard (NEW)

**File**: [webapp/src/test/java/.../TestDatabaseSafetyGuard.java](../webapp/src/test/java/com/saymyname/webapp/config/TestDatabaseSafetyGuard.java)

**Purpose**: Automatically rejects MySQL connections in test profile.

**Activation**: Runs BEFORE any database operation when `@ActiveProfiles("test")` is used.

**Protection Rules**:
```
✅ ALLOWED:   jdbc:h2:mem:...
✅ ALLOWED:   jdbc:h2:file:/tmp/... (not recommended)
❌ REJECTED:  jdbc:mysql://...
❌ REJECTED:  jdbc:postgresql://...
❌ REJECTED:  Any non-localhost database URL
```

**Error Example**:
```
╔══════════════════════════════════════════════════════════════════════╗
║                       🚨 TEST SAFETY VIOLATION 🚨                    ║
╠══════════════════════════════════════════════════════════════════════╣
║  Tests are attempting to connect to a MYSQL database!               ║
║  This is FORBIDDEN to prevent accidental data corruption.           ║
╠══════════════════════════════════════════════════════════════════════╣
║  Current configuration:                                              ║
║  - JDBC URL: jdbc:mysql://localhost:3306/saymyname                  ║
║  - Profile: test                                                     ║
║                                                                      ║
║  REQUIRED for tests:                                                 ║
║  - db.url=jdbc:h2:mem:testdb-[UNIQUE_ID]                            ║
║  - db.driver=org.h2.Driver                                           ║
╚══════════════════════════════════════════════════════════════════════╝
```

### 2. Isolated H2 Databases

**Configuration**:
```properties
# webapp/src/test/resources/application-test.properties
db.url=jdbc:h2:mem:test-saymyname-${random.uuid};MODE=MySQL;...
```

**Benefits**:
- ✅ Each test run gets a unique database instance
- ✅ No cross-contamination between test executions
- ✅ Parallel test execution safe
- ✅ Automatic cleanup (in-memory)

**Verification**:
```bash
# Run tests and check logs for unique DB name
mvn -pl webapp test | grep "JDBC URL"

# Expected output:
# JDBC URL: jdbc:h2:mem:test-saymyname-a1b2c3d4-e5f6-7890-1234-567890abcdef;MODE=MySQL;...
```

### 3. Profile-Based Configuration

**Test Profile Activation**:
```java
// In test classes
@SpringBootTest
@ActiveProfiles("test")  // ✅ Correct
class MyIntegrationTest {
```

**DO NOT** set `spring.profiles.active` in properties files:
```properties
# ❌ WRONG (removed from all test properties files)
spring.profiles.active=test

# ✅ CORRECT (use @ActiveProfiles in test classes)
# Note: spring.profiles.active should NOT be set in profile-specific properties
```

**Why**: Properties files can be overridden by environment variables, but `@ActiveProfiles` cannot.

---

## Tests That Boot Spring Context

### Current Inventory (2026-01-10)

| Test File | Type | Profile | Database | DDL Triggered |
|-----------|------|---------|----------|---------------|
| [WebappApplicationTest.java](../webapp/src/test/java/com/saymyname/webapp/WebappApplicationTest.java) | `@SpringBootTest` | `test` | H2 mem | ✅ Yes |
| [BaseWebIntegrationTest.java](../webapp/src/test/java/com/saymyname/webapp/config/BaseWebIntegrationTest.java) | `@SpringBootTest` (base class) | `test` | H2 mem | ✅ Yes |
| [BaseSecurityIntegrationTest.java](../webapp/src/test/java/com/saymyname/webapp/config/BaseSecurityIntegrationTest.java) | `@SpringBootTest` (base class) | `test` | H2 mem | ✅ Yes |

**All 3 tests**:
- ✅ Use `@ActiveProfiles("test")`
- ✅ Use `@TestPropertySource(locations = "classpath:application-test.properties")`
- ✅ Trigger Hibernate DDL with `hibernate.hbm2ddl.auto=create-drop`
- ✅ Protected by `TestDatabaseSafetyGuard`

### Finding New Spring Context Tests

```bash
# Search for Spring Boot test annotations
grep -r "@SpringBootTest\|@DataJpaTest\|@WebMvcTest" --include="*.java" .

# Search for tests that might boot ApplicationContext
grep -r "@ContextConfiguration\|ApplicationContext" --include="*Test.java" .
```

---

## How to Run Tests Safely

### Standard Test Execution

```bash
# Run all tests (safe - uses H2)
mvn clean test

# Run only webapp tests
mvn -pl webapp test

# Run specific test class
mvn -pl webapp test -Dtest=WebappApplicationTest

# Run with verbose logging to see database URL
mvn -pl webapp test | grep -E "JDBC URL|TEST DATABASE"
```

### Verification Checklist

After running tests, verify:

1. **Safety guard activated**:
   ```
   ✅ Look for: "===== TEST DATABASE SAFETY GUARD ACTIVATED ====="
   ✅ Look for: "✅ TEST DATABASE SAFETY CHECK PASSED"
   ```

2. **H2 database confirmed**:
   ```
   ✅ Look for: "JDBC URL: jdbc:h2:mem:test-saymyname-[UUID]"
   ✅ Look for: "JDBC Driver: org.h2.Driver"
   ```

3. **Unique database per run**:
   ```bash
   # Run twice and compare UUIDs
   mvn -pl webapp test | grep "test-saymyname"
   # Should show different UUIDs each time
   ```

### Emergency Override Detection

If someone tries to override via environment variables:

```bash
# This will FAIL (safety guard rejects MySQL)
export db.url=jdbc:mysql://localhost:3306/saymyname
mvn -pl webapp test

# Expected: Application fails to start with TEST SAFETY VIOLATION error
```

---

## Configuration Files

### Webapp Test Properties

**File**: [webapp/src/test/resources/application-test.properties](../webapp/src/test/resources/application-test.properties)

**Key Settings**:
```properties
# Database (custom DataSourceConfig uses db.* instead of spring.datasource.*)
db.driver=org.h2.Driver
db.url=jdbc:h2:mem:test-saymyname-${random.uuid};MODE=MySQL;...
db.username=sa
db.password=

# Hibernate
spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Service Test Properties

**File**: [service/src/test/resources/application-test.properties](../service/src/test/resources/application-test.properties)

**Note**: Service module tests don't boot Spring context (pure unit tests), but configuration exists for future integration tests.

---

## Troubleshooting

### Problem: Test connects to dev MySQL

**Symptoms**:
```
ERROR: Table 'saymyname.users' doesn't exist
```

**Diagnosis**:
```bash
# Check which database is being used
mvn -pl webapp test -X | grep -i "jdbc"
```

**Fix**:
1. Ensure `@ActiveProfiles("test")` on test class
2. Check for environment variables: `env | grep -i db`
3. Verify TestDatabaseSafetyGuard is in classpath

### Problem: Tests fail with "Table already exists"

**Symptoms**:
```
ERROR: Table 'users' already exists
```

**Cause**: Reusing same H2 database name across test runs

**Fix**:
```properties
# Ensure ${random.uuid} is in db.url
db.url=jdbc:h2:mem:test-saymyname-${random.uuid};...
```

### Problem: Safety guard doesn't activate

**Symptoms**: No "TEST DATABASE SAFETY GUARD" logs

**Diagnosis**:
```bash
# Check if TestDatabaseSafetyGuard is compiled
ls webapp/target/test-classes/com/saymyname/webapp/config/TestDatabaseSafetyGuard.class

# Check profile activation
mvn -pl webapp test -X | grep "Active profiles"
```

**Fix**:
1. Ensure test class has `@ActiveProfiles("test")`
2. Rebuild: `mvn clean compile test-compile`

### Problem: DDL errors in logs

**Symptoms**:
```
WARN: Syntax error in SQL statement "..."
```

**Diagnosis**: Check Hibernate dialect and H2 mode
```bash
mvn -pl webapp test | grep -E "dialect|MODE=MySQL"
```

**Expected**:
```
hibernate.dialect=org.hibernate.dialect.MySQLDialect
db.url=jdbc:h2:mem:...;MODE=MySQL;...
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run Tests (Safe - H2 only)
        run: mvn clean test

      - name: Verify H2 Usage
        run: |
          if grep -q "jdbc:mysql:" target/surefire-reports/*.txt; then
            echo "❌ DANGER: Tests connected to MySQL!"
            exit 1
          fi
          echo "✅ Tests used H2 only"
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'

                script {
                    // Verify no MySQL connections
                    def logs = readFile('target/surefire-reports/*.txt')
                    if (logs.contains('jdbc:mysql:')) {
                        error '❌ Tests connected to MySQL - ABORT'
                    }
                }
            }
        }
    }
}
```

---

## Best Practices

### DO ✅

- Use `@ActiveProfiles("test")` on all Spring context tests
- Verify test logs show H2 database before merging PR
- Run `mvn clean test` before push to validate safety
- Keep TestDatabaseSafetyGuard enabled (never disable)
- Use unique database names (`${random.uuid}`)

### DON'T ❌

- Set `spring.profiles.active=test` in properties files
- Override `db.url` via environment variables for tests
- Disable TestDatabaseSafetyGuard (@Profile("!test"))
- Use fixed database names (`testdb` instead of `testdb-${random.uuid}`)
- Run integration tests against dev/prod MySQL

---

## Environment Variable Override Protection

### How Override Works

Spring Boot property resolution order:
1. Environment variables (highest priority)
2. System properties (`-D` flags)
3. `application-test.properties`
4. `application.properties`

**DANGER**: Someone could do:
```bash
export db.url=jdbc:mysql://production-db:3306/saymyname
mvn test  # Would try to connect to production!
```

### Our Protection

`TestDatabaseSafetyGuard` validates the **effective** URL after all overrides:
```java
@Value("${db.url:NOT_SET}")  // Reads FINAL resolved value
private String jdbcUrl;

// Rejects if contains "jdbc:mysql:" regardless of source
if (jdbcUrl.contains("jdbc:mysql:")) {
    throw new IllegalStateException("TEST SAFETY VIOLATION");
}
```

**Result**: Even with environment variable override, test will FAIL FAST before any DDL execution.

---

## Schema Generation Logs

### Expected Logs (Safe)

```
2026-01-10 ... INFO  --- ===== TEST DATABASE SAFETY GUARD ACTIVATED =====
2026-01-10 ... INFO  --- Profile: test
2026-01-10 ... INFO  --- JDBC URL: jdbc:h2:mem:test-saymyname-a1b2c3d4...
2026-01-10 ... INFO  --- JDBC Driver: org.h2.Driver
2026-01-10 ... INFO  --- ✅ TEST DATABASE SAFETY CHECK PASSED
2026-01-10 ... DEBUG --- Hibernate: drop table if exists users cascade
2026-01-10 ... DEBUG --- Hibernate: drop table if exists organizations cascade
2026-01-10 ... DEBUG --- Hibernate: create table users (...) engine=InnoDB
2026-01-10 ... INFO  --- Initialized JPA EntityManagerFactory for persistence unit 'default'
```

### Dangerous Logs (REJECTED)

```
2026-01-10 ... INFO  --- ===== TEST DATABASE SAFETY GUARD ACTIVATED =====
2026-01-10 ... INFO  --- JDBC URL: jdbc:mysql://localhost:3306/saymyname
2026-01-10 ... ERROR ---
╔══════════════════════════════════════════════════════════════════════╗
║                       🚨 TEST SAFETY VIOLATION 🚨                    ║
║  Tests are attempting to connect to a MYSQL database!               ║
╚══════════════════════════════════════════════════════════════════════╝

2026-01-10 ... ERROR --- Application run failed
java.lang.IllegalStateException: TEST SAFETY VIOLATION
```

---

## Testing the Safety Guard

### Simulate Production Override Attack

```bash
# Step 1: Set dangerous environment variable
export db_url=jdbc:mysql://localhost:3306/saymyname

# Step 2: Try to run tests
mvn -pl webapp test

# Step 3: Verify safety guard REJECTS it
# Expected: IllegalStateException with TEST SAFETY VIOLATION

# Step 4: Clean up
unset db_url
```

### Verify Unique Database Per Run

```bash
# Run 1
mvn -pl webapp test | grep "JDBC URL" | tee run1.log

# Run 2
mvn -pl webapp test | grep "JDBC URL" | tee run2.log

# Compare UUIDs (should be different)
diff run1.log run2.log
```

---

## Summary

### Protection Layers

1. **Layer 1**: `@ActiveProfiles("test")` in test classes → Activates test configuration
2. **Layer 2**: `application-test.properties` → Configures H2 with unique name
3. **Layer 3**: `TestDatabaseSafetyGuard` → Fail-fast validation (rejects MySQL)
4. **Layer 4**: No `spring.profiles.active` in properties → Prevents accidental override

### Risk Matrix

| Scenario | Protected? | Result |
|----------|------------|--------|
| Normal test run | ✅ Yes | Uses H2, passes |
| Env var override to MySQL | ✅ Yes | Safety guard fails fast |
| Forgot `@ActiveProfiles` | ⚠️ Partial | Uses default profile (dev), may connect to MySQL |
| Disabled safety guard | ❌ No | Unprotected |
| Removed `@ActiveProfiles` AND env override | ❌ No | Dangerous |

**Mitigation for unprotected scenarios**:
- Code review: Check all `@SpringBootTest` have `@ActiveProfiles("test")`
- CI pipeline: Verify test logs contain H2 URLs
- Pre-commit hook: Scan for `@SpringBootTest` without `@ActiveProfiles`

---

**Last Updated**: 2026-01-10
**Author**: Claude Sonnet 4.5
**Status**: ✅ Production Ready
