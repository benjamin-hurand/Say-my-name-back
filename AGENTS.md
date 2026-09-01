# AGENTS.md — SayMyName Backend

This file defines the working rules for AI coding agents operating on the SayMyName backend repository.

The rules in this document apply by default unless the user explicitly gives different instructions for a specific task.

---

## 1. Project context

SayMyName is an interactive quiz application designed to help users learn and memorize people's names and other attributes.

Backend stack:

* Java 21
* Spring Boot 3.3.1
* Hibernate / JPA 6.5.2
* MySQL 8.0.41
* Maven
* Flyway 10.10.0
* Testcontainers
* REST APIs
* OAuth2 / JWT

The backend is split into Maven modules. Respect existing module boundaries and architecture before introducing new dependencies or moving responsibilities between modules.

---

# 2. Database source of truth

Flyway is the source of truth for the database schema.

Hibernate MUST NOT create, update, or mutate the schema.

Expected Hibernate schema policy:

```properties
spring.jpa.properties.hibernate.hbm2ddl.auto=none
```

Schema compatibility must be verified using Hibernate `validate` in appropriate tests or validation flows.

Never solve a schema mismatch by enabling:

* `create`
* `create-drop`
* `update`

unless the user explicitly requests a temporary isolated experiment.

---

# 3. Flyway migration rules

Existing migrations are immutable.

In particular:

* `V1__initial_schema.sql` MUST NOT be modified.
* `V2__add_workspace_tenant_unique_key.sql` MUST NOT be modified.

Any new schema evolution must use a new migration:

```text
V3__description.sql
V4__description.sql
V5__description.sql
...
```

Never edit an already applied versioned migration to make a new change.

Never renumber existing migrations.

Before creating a migration:

1. inspect the current schema;
2. inspect existing Flyway migrations;
3. inspect the related JPA entities and mappings;
4. determine whether the change is really a database change.

Migration filenames must clearly describe their purpose.

Prefer one coherent schema change per migration.

---

# 4. Database accounts and permissions

The local development database uses separate MySQL identities.

## Runtime application

```text
saymyname_app
```

Allowed:

* SELECT
* INSERT
* UPDATE
* DELETE

Not allowed:

* CREATE
* ALTER
* DROP
* schema administration

Used by Spring Boot runtime.

---

## Agent / inspection account

```text
saymyname_readonly
```

Allowed:

* SELECT

Not allowed:

* INSERT
* UPDATE
* DELETE
* CREATE
* ALTER
* DROP

This is the default database identity for AI agents.

Agents MUST use readonly access when inspecting the real local database.

---

## Flyway migrator

```text
saymyname_migrator
```

This account has schema migration permissions.

It exists exclusively for controlled Flyway execution.

Agents MUST NOT use this account by default.

---

# 5. Agent database access

To inspect the local database, use:

```powershell
.\scripts\agent-db.ps1 -Query "<SELECT query>"
```

This script uses the readonly database account.

Agents may freely use SELECT queries when needed to understand:

* tables;
* columns;
* indexes;
* foreign keys;
* constraints;
* existing data shape;
* Flyway history;
* relationships.

Agents must prefer metadata queries and targeted SELECT queries.

Avoid unnecessarily reading large amounts of user data.

Never expose passwords, secrets, tokens, or private database values in responses or logs.

---

# 6. Flyway validation

The default Flyway validation command is:

```powershell
.\scripts\flyway-validate.ps1
```

This command uses readonly credentials.

Agents may run it without requesting additional authorization.

Its purpose is to verify that:

* migration files are valid from Flyway's perspective;
* migration checksums match;
* the database migration history is consistent with the repository.

`flyway:validate` does NOT replace actual migration testing.

---

# 7. Testing new migrations

New migrations must be tested against an ephemeral database before being applied to the real local database.

The expected validation flow is:

```text
empty MySQL 8.0.41 database
        ↓
Flyway V1
        ↓
Flyway V2
        ↓
new migration(s)
        ↓
Hibernate validate
        ↓
Spring context
        ↓
integration assertions
```

Use the existing Testcontainers infrastructure.

Current reference database version for Testcontainers:

```text
MySQL 8.0.41
```

Agents are allowed to execute Testcontainers-based tests because the database is isolated and ephemeral.

When changing database structure, prefer running the relevant integration test(s), including `WebappApplicationIT` when appropriate.

A successful `flyway:validate` alone is not sufficient proof that a new migration works on a fresh database.

---

# 8. Real database migrations — protected operation

Applying migrations to the real local `saymyname` database is a protected operation.

The migration command is:

```powershell
.\scripts\flyway-migrate.ps1
```

Agents MUST NOT run this command automatically.

Agents may run it only when the user explicitly asks to:

* apply the migration;
* migrate the real/local database;
* execute Flyway migrate;
* or gives another clearly equivalent authorization.

Creating a migration file is NOT authorization to execute it.

Running tests is NOT authorization to execute it.

Running `flyway-validate.ps1` is NOT authorization to execute it.

When in doubt, do not migrate the real database.

---

# 9. Production database policy

AI agents must assume that direct production database access is forbidden.

Agents must never:

* connect directly to the production database;
* run migrations manually against production;
* request production database credentials for normal development;
* embed production secrets in scripts;
* create tooling that silently targets production.

The intended production architecture is:

```text
CI/CD migration job
        ↓
production migrator identity
        ↓
Flyway migrations

application runtime
        ↓
production app identity
```

Production migrations must eventually be controlled by CI/CD and explicit deployment procedures.

---

# 10. Secrets

Local secrets are stored outside version-controlled configuration.

Current local convention:

```text
.secrets.local.ps1
```

This file must remain gitignored.

It may define variables such as:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

FLYWAY_URL
FLYWAY_USERNAME
FLYWAY_PASSWORD

AGENT_DB_USERNAME
AGENT_DB_PASSWORD
```

Never:

* commit this file;
* print passwords;
* copy secrets into documentation;
* hardcode credentials into Maven configuration;
* hardcode credentials into PowerShell scripts;
* put real secrets into test fixtures.

Application configuration should resolve sensitive values through environment variables.

If a secret is discovered in a tracked file, report it instead of silently propagating it.

---

# 11. SQL migration quality

Schema migrations should be explicit and deterministic.

Prefer:

* explicit constraints;
* explicit indexes;
* explicit foreign keys;
* descriptive constraint names where practical;
* changes compatible with MySQL 8.0.41.

Avoid relying on ORM auto-generation for production schema changes.

Before adding an index, inspect existing indexes.

Before adding a constraint, inspect existing constraints.

Before removing or changing a column, search the complete backend for:

* JPA mappings;
* DTOs;
* repositories;
* Criteria API usage;
* native SQL;
* services;
* tests.

For destructive migrations, do not assume data can safely be discarded.

If data transformation is required, reason about existing rows before defining `NOT NULL`, unique constraints, type narrowing, or column deletion.

---

# 12. JPA / Hibernate rules

Database migrations and JPA mappings must remain synchronized.

When modifying an entity:

* verify column names;
* verify nullability;
* verify lengths;
* verify enum storage;
* verify relationships;
* verify foreign key assumptions;
* verify indexes or uniqueness when relevant.

Avoid fixing lazy-loading problems by globally enabling Open Session in View.

Current application policy:

```properties
spring.jpa.open-in-view=false
```

Prefer explicit transactional service boundaries and appropriate fetching strategies.

---

# 13. DTO and architecture boundaries

Do not expose JPA entities directly through REST APIs unless the existing architecture explicitly does so for that area.

Prefer the project's existing pattern:

```text
Entity ↔ Mapper ↔ DTO ↔ REST
```

Keep:

* persistence concerns in persistence/repository layers;
* business rules in services;
* HTTP concerns in controllers/web layer.

Before creating a new abstraction, inspect whether an equivalent pattern already exists in the project.

Avoid unnecessary architectural rewrites while implementing targeted features.

---

# 14. Change discipline

For each task:

1. inspect the relevant existing implementation;
2. understand the current behavior;
3. make the smallest coherent change;
4. update tests where needed;
5. run the most relevant validation;
6. report exactly what changed and what was verified.

Do not modify unrelated files merely for cleanup.

Do not perform broad refactors unless they are required for the requested task or explicitly requested.

Preserve existing behavior unless the requested change intentionally modifies it.

---

# 15. Test discipline

Do not claim a change is validated unless the corresponding command or test actually ran successfully.

Differentiate clearly between:

```text
implemented
compiled
unit-tested
integration-tested
migration-tested
manually verified
```

When a test fails:

* inspect the actual error;
* identify whether the failure is caused by the current change;
* do not weaken assertions merely to make the test pass.

Tests using ephemeral Testcontainers databases are safe to execute without explicit migration authorization.

---

# 16. Maven

Prefer running Maven commands against the narrowest relevant module when possible.

Existing Maven warnings should not automatically be mixed into unrelated changes.

Do not upgrade:

* Java;
* Spring Boot;
* Hibernate;
* Flyway;
* Maven plugins;
* MySQL versions

as part of an unrelated task.

Dependency or build-system upgrades must be treated as explicit changes.

---

# 17. Git safety

Agents may inspect Git state and diffs.

Before changing files, check relevant existing modifications when necessary to avoid overwriting user work.

Never automatically:

* discard user changes;
* run destructive reset commands;
* force checkout files;
* rewrite Git history;
* force push.

Do not commit database dumps containing real data.

Do not commit:

* `.secrets.local.ps1`;
* credentials;
* access tokens;
* private keys;
* production configuration secrets.

Existing migration files already committed and applied must remain immutable.

---

# 18. Database dumps

Database dumps may contain real or sensitive data.

Treat them as local artifacts by default.

Do not add database dumps to Git unless the user explicitly requests it and their contents have been verified safe.

Schema-only fixtures are preferable when repository test data is required.

---

# 19. Default workflow for a database change

Unless the user explicitly requests another workflow, use this sequence:

```text
1. Inspect code and current migrations
2. Inspect real schema with readonly access if useful
3. Design the database change
4. Create the next Flyway migration
5. Update JPA/entities/code
6. Run flyway-validate.ps1
7. Run relevant Testcontainers integration tests
8. Review resulting diff
9. Stop
```

At step 9 the real local database remains unchanged.

Only after explicit user authorization:

```text
10. Run flyway-migrate.ps1
11. Verify migration result
12. Verify application startup / Hibernate validation
```

---

# 20. Current Flyway baseline

Current established migration history:

```text
V1__initial_schema.sql
V2__add_workspace_tenant_unique_key.sql
```

The historical `saymyname` database was onboarded into Flyway using an existing-schema baseline.

Current schema version:

```text
2
```

The next versioned database change must therefore start at:

```text
V3__
```

unless newer migrations already exist when the agent performs the task.

Always inspect the migration directory before choosing the next version number.

---

# 21. Legacy administrator account

A historical MySQL account with broad privileges still exists:

```text
adminsaymyname@%
```

It must NOT be used by agents.

Do not modify or remove it unless the user explicitly requests the security cleanup phase.

The intended long-term accounts are:

```text
saymyname_readonly
saymyname_app
saymyname_migrator
```

---

# 22. Decision priority

When multiple approaches are possible, prioritize:

1. data safety;
2. reproducible migrations;
3. least privilege;
4. automated verification;
5. compatibility with existing architecture;
6. simple maintainable implementation.

When a potentially destructive action is not necessary, choose the non-destructive alternative.
