# SayMyName — Backend

SayMyName est une application de quiz interactive destinée à aider ses utilisateurs à apprendre et mémoriser des noms de personnes et d'autres attributs (visages, informations, etc.).

Ce dépôt contient le backend de l'application : une API Spring Boot multi-modules en Java 21.

## Stack technique

- Java 21
- Spring Boot 3.3.1
- Hibernate / JPA 6.5.2
- MySQL 8.0.41
- Maven (multi-modules)
- Flyway 10.10.0
- Testcontainers
- REST API, OAuth2 / JWT

## Architecture & modules Maven

Le projet est un build Maven multi-modules (`pom.xml` racine en `packaging=pom`). Les modules ont des dépendances en couches :

```
webapp  ──> service, security, infra
service ──> core, persistence, security
infra   ──> core, service          (implémentations d'infrastructure, ex. mail)
security ─> core                   (JWT, OAuth2 Google)
persistence ─> core                (entités JPA, repositories, mappers, DAO)
core    ──> (base, aucune dépendance interne)
```

| Module        | Rôle |
|---------------|------|
| `core`        | Modèle de domaine, exceptions métier, validation, utilitaires partagés — aucune dépendance vers les autres modules internes. |
| `persistence` | Entités JPA, repositories Spring Data, DAO, mappers Entity↔DTO, configuration multi-tenant, stockage. |
| `security`    | Authentification JWT, intégration OAuth2 Google, utilitaires de sécurité. |
| `service`     | Logique métier (quiz, cours, invitations, leaderboard, profils, etc.), orchestration des cas d'usage. |
| `infra`       | Implémentations d'infrastructure consommées par `service` (ex. envoi d'e-mails). |
| `webapp`      | Application Spring Boot exécutable : controllers REST, DTO, mappers web, configuration Spring, migrations Flyway, point d'entrée (`WebappApplication`). |

Le module `webapp` est le seul packagé en JAR exécutable (`spring-boot-maven-plugin`, `repackage`).

## Prérequis

- JDK 21
- Maven (testé avec Maven 3.9.9 — aucun wrapper `mvnw` n'est fourni dans ce dépôt, Maven doit être installé sur la machine)
- MySQL 8.0.41 accessible localement
- Docker (requis pour les tests d'intégration via Testcontainers)
- Client `mysql` en ligne de commande (utilisé par `scripts/agent-db.ps1`)
- PowerShell (les scripts du dépôt sont écrits pour PowerShell — l'environnement de développement principal est Windows)

## Configuration locale

L'application ne lit aucun secret en dur : toutes les valeurs sensibles viennent de variables d'environnement.

### Variables d'environnement

| Variable | Utilisée par | Rôle |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Runtime Spring Boot | Connexion applicative (compte `saymyname_app`) |
| `FLYWAY_URL`, `FLYWAY_USERNAME`, `FLYWAY_PASSWORD` | Flyway (`scripts/flyway-migrate.ps1`) | Exécution des migrations (compte `saymyname_migrator`) |
| `AGENT_DB_USERNAME`, `AGENT_DB_PASSWORD` | `scripts/agent-db.ps1`, `scripts/flyway-validate.ps1` | Accès lecture seule (compte `saymyname_readonly`) |
| `SPRING_PROFILES_ACTIVE` | Spring Boot | Profil actif (`dev` en local, `prod` en production) — ne jamais forcer `dev` dans `application.properties` |

### `.secrets.local.ps1`

Pour le développement local, ces variables sont fournies via un fichier `\.secrets.local.ps1` à la racine du dépôt. Ce fichier est **gitignoré** et ne doit jamais être commité.

Exemple de structure attendue (valeurs fictives à adapter) :

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/saymyname"
$env:DB_USERNAME = "saymyname_app"
$env:DB_PASSWORD = "<mot-de-passe-local>"

$env:FLYWAY_URL = "jdbc:mysql://localhost:3306/saymyname"
$env:FLYWAY_USERNAME = "saymyname_migrator"
$env:FLYWAY_PASSWORD = "<mot-de-passe-local>"

$env:AGENT_DB_USERNAME = "saymyname_readonly"
$env:AGENT_DB_PASSWORD = "<mot-de-passe-local>"
```

Les scripts (`scripts/flyway-common.ps1`, `scripts/agent-db.ps1`) chargent automatiquement `.secrets.local.ps1` s'il existe. Le fichier peut aussi être chargé automatiquement à l'ouverture d'un terminal PowerShell dans le dossier du backend via le profil PowerShell — voir [docs/database-development.md](docs/database-development.md#5-automatic-powershell-secret-loading) pour la configuration exacte.

Ces mêmes noms de variables sont réutilisés tels quels dans tous les environnements (local, dev, prod à terme) : c'est l'environnement d'exécution qui fournit les bonnes valeurs, jamais le code.

### Lancer le backend en local

Une fois `.secrets.local.ps1` chargé dans la session PowerShell :

```powershell
mvn clean install -U
java -jar webapp/target/webapp-1.0-SNAPSHOT.jar
```

Ou en une seule ligne :

```powershell
mvn clean install -U; if ($LastExitCode -eq 0) { java -jar webapp/target/webapp-1.0-SNAPSHOT.jar }
```

Par défaut l'application écoute sur `0.0.0.0:8080`. Pour activer le profil de développement (logs plus verbeux, mailer console) :

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
```

## Commandes Maven utiles

| Commande | Effet |
|---|---|
| `mvn validate` | Vérifie la configuration du build (pas de warning sur les versions de plugins) |
| `mvn clean install -U` | Build complet du multi-module + tests unitaires (les tests d'intégration sont ignorés par défaut) |
| `mvn test -pl <module> -am` | Tests unitaires d'un seul module (ex. `-pl service`) |
| `mvn verify -Pit` | Active les tests d'intégration (`*IT.java`, `*IntegrationTest.java`) via Failsafe, en plus des tests unitaires |
| `mvn clean install -Pskip-tests` | Build sans exécuter aucun test |
| `mvn verify -Pcoverage` | Génère un rapport de couverture JaCoCo agrégé |

Les profils Maven disponibles (définis dans le `pom.xml` racine) sont : `tests`, `it`, `skip-tests`, `coverage`.

## Base de données & Flyway

Flyway est la source de vérité du schéma ; Hibernate est configuré en `hibernate.hbm2ddl.auto=none` et ne doit jamais créer ou modifier le schéma. Les migrations versionnées vivent dans `webapp/src/main/resources/db/migration/` et sont immuables une fois appliquées.

Workflow résumé :

1. Inspecter le schéma et les migrations existantes.
2. Créer une nouvelle migration `V{n}__description.sql`.
3. Mettre à jour les entités JPA concernées.
4. Valider avec `scripts/flyway-validate.ps1` (lecture seule, sans risque).
5. Tester la migration via les tests d'intégration Testcontainers.
6. Appliquer la migration sur la base locale uniquement après validation, avec `scripts/flyway-migrate.ps1 -Environment local`.

Cette dernière commande est une opération protégée : `-Environment prod` échoue volontairement, la production n'étant migrée qu'via un futur pipeline CI/CD dédié.

Le détail complet (comptes MySQL séparés par usage, isolation Testcontainers, historique des migrations, règles de sécurité) est documenté dans **[docs/database-development.md](docs/database-development.md)** — c'est la référence à consulter avant toute évolution de schéma.

## Tests

- **Tests unitaires** : fichiers `*Test.java` / `*Tests.java`, présents dans chaque module (`core`, `persistence`, `security`, `service`, `infra`, `webapp`). Basés sur JUnit 5, Mockito et AssertJ. Exécutés par défaut avec `mvn test` ou `mvn clean install`.
- **Tests d'intégration** : fichiers `*IT.java` / `*IntegrationTest.java` (ex. `WebappApplicationIT`), exécutés via Failsafe avec le profil `-Pit`. Ils démarrent un conteneur MySQL 8.0.41 éphémère via Testcontainers (`TestcontainersConfiguration`) sur lequel Flyway applique les migrations avant que Hibernate ne valide le mapping (`hbm2ddl.auto=validate`).
- L'isolation Testcontainers est stricte : la datasource applicative **et** Flyway sont explicitement repointés vers le conteneur, jamais vers une base réelle (`DB_URL`/`FLYWAY_URL`).

## Scripts

| Script | Usage | Notes |
|---|---|---|
| `scripts/agent-db.ps1 -Query "<SQL>"` | Requêtes lecture seule sur la base locale | Utilise le compte `saymyname_readonly` |
| `scripts/flyway-validate.ps1` | Valide les migrations (checksums, historique) | Lecture seule, sans risque |
| `scripts/flyway-migrate.ps1 -Environment local\|dev` | Applique les migrations | Opération protégée, `prod` bloqué explicitement |
| `scripts/flyway-common.ps1` | Fonctions partagées par les scripts Flyway | Non destiné à un appel direct |

## Agents IA (Claude Code, Codex, etc.)

Ce dépôt définit des règles strictes pour les agents IA qui interviennent sur le code :

- **[AGENTS.md](AGENTS.md)** est la source de vérité pour l'accès base de données, les règles de migration Flyway, la gestion des secrets, les comptes MySQL et la discipline de changement.
- **[CLAUDE.md](CLAUDE.md)** indique à Claude Code de toujours lire et respecter `AGENTS.md` avant toute modification.

Tout contributeur (humain ou agent) travaillant sur la base de données doit lire `AGENTS.md` en premier.

## Structure du dépôt (simplifiée)

```
Backend/
├── core/            # Domaine métier, exceptions, validation (base commune)
├── persistence/      # Entités JPA, repositories, DAO, mappers
├── security/          # JWT, OAuth2 Google
├── service/            # Logique métier / cas d'usage
├── infra/               # Implémentations d'infrastructure (mail, ...)
├── webapp/               # Application Spring Boot (controllers, DTO, migrations Flyway, point d'entrée)
├── scripts/               # Scripts PowerShell (Flyway, inspection DB)
├── docs/                   # Documentation complémentaire
├── AGENTS.md               # Règles pour les agents IA
├── CLAUDE.md                # Pointeur vers AGENTS.md pour Claude Code
└── pom.xml                   # POM parent (multi-modules)
```

## Documentation

- [docs/database-development.md](docs/database-development.md) — workflow complet base de données / Flyway / Testcontainers.
- D'autres documents pourront être ajoutés dans `docs/` au fur et à mesure (architecture détaillée, déploiement, etc.) ; ce README doit alors être mis à jour pour les référencer.

## Travaux futurs

D'après `AGENTS.md` et `docs/database-development.md`, les éléments suivants ne sont **pas encore implémentés** et ne doivent pas être présentés comme opérationnels :

- pipeline CI/CD (y compris pour les migrations de production) ;
- environnements/secrets de production ;
- provisioning de la base de données sur AWS ;
- comptes MySQL dédiés à la production (`migrator`/`app` de prod) ;
- suppression du compte administrateur historique `adminsaymyname@%`.

Le workflow local actuel (scripts, Testcontainers, comptes MySQL séparés en local) est conçu pour rester compatible avec ces évolutions futures sans redesign majeur.
