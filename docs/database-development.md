# Database development — SayMyName

This document describes the local database development workflow for SayMyName.

Its purpose is to document:

- Flyway migration rules;
- local database accounts;
- agent database access;
- migration validation;
- Testcontainers isolation;
- local secrets;
- local migration execution;
- current safety rules.

---

## 1. Stack

Current database-related stack:

- MySQL 8.0.41
- Flyway 10.10.0
- Hibernate / JPA 6.5.2
- Spring Boot 3.3.1
- Testcontainers
- Maven
- PowerShell scripts for local workflows

Flyway is the source of truth for the database schema.

Hibernate must not create or update the schema automatically.

Current Hibernate policy:

spring.jpa.properties.hibernate.hbm2ddl.auto=none

Hibernate validation is used to check consistency between mappings and the database schema.

---

## 2. Flyway migration rules

Existing versioned migrations are immutable once applied.

Current migration history includes:

- V1__initial_schema.sql
- V2__add_workspace_tenant_unique_key.sql
- V3__add_tenants_migration_test_note.sql
- V4__drop_tenants_migration_test_note.sql

V3 and V4 were temporary validation migrations used to test the migration workflow.

They remain part of the permanent Flyway history because V3 was applied to the persistent local database and both migrations were subsequently validated.

Never:

- modify an already applied migration;
- rename an already applied migration;
- renumber migrations;
- fix a schema change by editing V1, V2, V3, etc.

Any future schema evolution must use the next available Flyway version.

Always inspect the migration directory before choosing a version number.

---

## 3. Database accounts

Local MySQL access is split by responsibility.

### Application runtime

User:

saymyname_app

Permissions:

- SELECT
- INSERT
- UPDATE
- DELETE

Not allowed:

- CREATE
- ALTER
- DROP
- schema administration

This account is used by the Spring application at runtime.

---

### Agent / readonly access

User:

saymyname_readonly

Permissions:

- SELECT

Not allowed:

- INSERT
- UPDATE
- DELETE
- CREATE
- ALTER
- DROP

This is the default account for Claude Code, Codex, and other inspection tools.

Agents should inspect the real local database using readonly access.

---

### Flyway migrator

User:

saymyname_migrator

This account has the schema permissions required by Flyway.

It must only be used for controlled migration execution.

Agents must not use it by default.

---

### Legacy administrator

The historical account:

adminsaymyname@%

still exists with broad privileges.

It must not be used by agents.

It must not be removed or reduced until the database security migration is considered complete.

---

## 4. Local secrets

Local secrets are stored in:

.secrets.local.ps1

This file is gitignored and must never be committed.

Current environment variable convention:

DB_URL
DB_USERNAME
DB_PASSWORD

FLYWAY_URL
FLYWAY_USERNAME
FLYWAY_PASSWORD

AGENT_DB_USERNAME
AGENT_DB_PASSWORD

The project uses the same variable names across environments.

The values are provided by the execution environment.

Future CI/CD environments should inject the same variables using protected secret storage.

---

## 5. Automatic PowerShell secret loading

Local secrets are automatically loaded when PowerShell starts inside the SayMyName backend directory.

PowerShell profile configuration:

$sayMyNameBackend = "C:\Dev\Projects\SayMyName\Backend"
$sayMyNameSecrets = Join-Path $sayMyNameBackend ".secrets.local.ps1"

$currentPath = (Get-Location).Path

if (
    $currentPath -eq $sayMyNameBackend -or
    $currentPath.StartsWith("$sayMyNameBackend\")
) {
    if (Test-Path $sayMyNameSecrets) {
        . $sayMyNameSecrets
    }
}

This provides automatic local convenience without loading SayMyName secrets into unrelated PowerShell sessions.

Important:

- do not commit the PowerShell profile;
- do not hardcode passwords in the profile;
- only the path to the local secrets file is stored there.

---

## 6. Readonly database inspection

Use:

.\scripts\agent-db.ps1 -Query "<SQL>"

Example:

.\scripts\agent-db.ps1 -Query "SELECT DATABASE(), CURRENT_USER();"

Expected identity:

saymyname_readonly@localhost

The script:

- reads DB_URL;
- parses the MySQL host, port and database;
- uses AGENT_DB_USERNAME;
- uses AGENT_DB_PASSWORD;
- uses a temporary MySQL defaults file;
- does not pass the password directly on the command line;
- deletes the temporary file after execution.

Agents should prefer targeted metadata queries and SELECT queries.

Avoid unnecessarily reading large volumes of application data.

---

## 7. Flyway validation

Use:

.\scripts\flyway-validate.ps1

This command:

- uses FLYWAY_URL;
- authenticates using AGENT_DB_USERNAME / AGENT_DB_PASSWORD;
- runs flyway:validate;
- does not modify the schema.

A successful validation confirms that:

- migration checksums match;
- migration history is coherent;
- Flyway recognizes the migration files.

It does not prove that a pending migration can be applied successfully.

---

## 8. Testing migrations

Every new migration must be tested with Testcontainers before being applied to the real local database.

Expected flow:

empty MySQL 8.0.41 database
        ↓
Flyway migrations
        ↓
Hibernate validate
        ↓
Spring context
        ↓
integration assertions

Current integration test infrastructure uses MySQL 8.0.41.

A new migration should normally be validated by:

1. creating the migration;
2. updating JPA mappings if required;
3. running Flyway validation;
4. running the relevant Testcontainers integration tests;
5. reviewing the resulting diff;
6. stopping before changing the persistent local database.

---

## 9. Testcontainers isolation

Testcontainers must never access or migrate the real local database.

Both runtime database configuration and Flyway configuration must explicitly target the ephemeral container.

The test configuration must override:

- runtime database URL;
- runtime database username;
- runtime database password;
- spring.flyway.url;
- spring.flyway.user;
- spring.flyway.password.

Custom application database properties such as:

db.url
db.username
db.password

must also resolve to the container when tests run.

Tests must not inherit a real database URL from:

- application.properties;
- DB_URL;
- FLYWAY_URL;
- .secrets.local.ps1;
- developer shell configuration;
- CI/CD environment variables.

### Historical isolation incident

During migration workflow validation, WebappApplicationIT originally redirected the application datasource to Testcontainers but did not override spring.flyway.url/user/password.

As a result, Flyway inherited the real local FLYWAY_URL and applied V3 to the persistent local database.

The issue was detected through readonly inspection and corrected by explicitly pointing Flyway to the Testcontainers database.

After the fix:

- Testcontainers migrations run only against the ephemeral database;
- repeated integration test runs no longer modify the real flyway_schema_history.

This incident is the reason explicit Flyway test isolation is now required.

---

## 10. Applying a migration locally

Agents stop after validation unless the user explicitly authorizes migration of the persistent local database.

When a migration has been reviewed and accepted, apply it with:

.\scripts\flyway-migrate.ps1 -Environment local

This uses:

FLYWAY_URL
FLYWAY_USERNAME
FLYWAY_PASSWORD

The migrator account is therefore only used during explicit migration execution.

After migration, verify the result with readonly access.

Example:

.\scripts\agent-db.ps1 -Query "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

Then start or validate the application to ensure Hibernate mappings remain compatible.

---

## 11. Migration environment protection

The developer migration script accepts:

- local
- dev
- prod

However:

prod is explicitly blocked.

The following command must fail:

.\scripts\flyway-migrate.ps1 -Environment prod

Production migrations are intended to run through a dedicated CI/CD migration workflow in the future.

Agents must not:

- remove this protection;
- bypass it;
- call Maven Flyway directly to work around it;
- use production migrator credentials manually.

No CI/CD implementation is required yet.

The current scripts are simply designed so that future CI/CD integration will not require redesigning the local database workflow.

---

## 12. Normal agent workflow

For a database-related task, the expected agent workflow is:

1. inspect existing code;
2. inspect current migrations;
3. inspect the real database using readonly access if useful;
4. create the next Flyway migration;
5. update JPA / application code if necessary;
6. run flyway-validate.ps1;
7. run relevant Testcontainers tests;
8. review the diff;
9. stop.

At this point the persistent local database must remain unchanged.

Only after explicit user approval:

10. run flyway-migrate.ps1 -Environment local;
11. verify Flyway history;
12. verify schema;
13. start or validate the application.

---

## 13. Starting the backend locally

Current full build and run command:

mvn clean install -U; if ($LastExitCode -eq 0) { java -jar webapp/target/webapp-1.0-SNAPSHOT.jar }

The PowerShell session must have the required DB and Flyway environment variables available.

With the local PowerShell profile configured correctly, these variables are loaded automatically when the terminal starts inside the Backend directory.

---

## 14. Maven build configuration

The root Maven project explicitly manages the maven-compiler-plugin version.

This avoids relying on an implicit Maven plugin version and keeps builds reproducible.

Validation command:

mvn validate

The build should not emit:

'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing

---

## 15. Files related to database development

Important files:

AGENTS.md
CLAUDE.md

scripts/agent-db.ps1
scripts/flyway-common.ps1
scripts/flyway-validate.ps1
scripts/flyway-migrate.ps1

webapp/src/main/resources/application.properties

webapp/src/main/resources/db/migration/

webapp/src/test/...
TestcontainersConfiguration
WebappApplicationIT

.secrets.local.ps1
(local only, gitignored)

---

## 16. Future work

Not implemented yet:

- CI/CD migration jobs;
- GitHub Environments;
- production secret storage;
- AWS database provisioning;
- production migrator account;
- production runtime account;
- automated deployment;
- reduction/removal of adminsaymyname;
- broader database backup/restore automation;
- full project documentation.

The current local workflow is intentionally designed to remain compatible with these future improvements.