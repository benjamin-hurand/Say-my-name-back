# Plan de refactoring — Standardisation Lombok/JPA

> Périmètre : entities JPA (`persistence/...entity`) + models core (`core/...model`)
> Hors périmètre : controllers, services, repositories, mappers, SQL/migrations, tests
> Date : 2026-02-20

---

## Résumé cible

Le codebase est **largement déjà conforme** à la cible. Les **core models** (20 classes) utilisent tous `@Value + @Builder(toBuilder=true)` : **rien à faire**. Les **entities** (49 classes) : 46 suivent déjà le safe JPA profile (`@Getter @Setter @NoArgsConstructor(PROTECTED) @AllArgsConstructor(PRIVATE) @SuperBuilder/@Builder @EqualsAndHashCode(onlyExplicitlyIncluded) @ToString(onlyExplicitlyIncluded)`).

Seules **3 entities** sont non-conformes :
- `UserEntity` — zéro Lombok, tout manuel
- `UserIdentityEntity` — zéro Lombok, tout manuel
- `UserEmailEntity` — Lombok partiel mal configuré + `@SuperBuilder` injustifié

`BaseTenantScoped` est correct. Les 6 classes `@Embeddable` (`UserOrganizationId`, etc.) ont `@EqualsAndHashCode` sans `onlyExplicitlyIncluded` : **correct** pour des value objects composites. Budget de migration : **3 fichiers entities** à modifier, 0 core model.

---

## 1. Audit de l'existant

### 1.1 Patterns Lombok — Entities

| Classe | Lombok actuel | Conforme ? | Risques |
|--------|--------------|-----------|---------|
| `UserEntity` | **Aucun** (manuel) | ❌ | `toString` absent (Object default), equals/hashCode manuels OK (id-based), setters custom sur collections bidi |
| `UserIdentityEntity` | **Aucun** (manuel) | ❌ | equals/hashCode manuels OK (id-based), `isLocal()` helper manuel |
| `UserEmailEntity` | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder` | ❌ | `@SuperBuilder` sans héritage (doit être `@Builder`), `@AllArgsConstructor` sans `AccessLevel.PRIVATE`, pas de `@EqualsAndHashCode`, pas de `@ToString`, equals/hashCode manuels redondants |
| `BaseTenantScoped` | `@Getter @Setter @NoArgsConstructor(PROTECTED) @SuperBuilder @ToString(onlyExplicitlyIncluded)` | ✅ | Pas de `@EqualsAndHashCode` sur la superclasse — correct car chaque enfant définit le sien |
| `UserRefreshTokenEntity` | `@Getter @Setter @NoArgsConstructor(PROTECTED) @AllArgsConstructor(PRIVATE) @Builder @EqualsAndHashCode(onlyExplicitlyIncluded) @ToString(onlyExplicitlyIncluded)` | ✅ | — |
| Entities `@SuperBuilder` (23 classes) | `@Getter @Setter @NoArgsConstructor(PROTECTED) @AllArgsConstructor(PRIVATE) @SuperBuilder @EqualsAndHashCode(onlyExplicitlyIncluded) @ToString(onlyExplicitlyIncluded)` | ✅ | — |
| Entities `@Builder` simples (20 classes) | `@Getter @Setter @NoArgsConstructor(PROTECTED) @AllArgsConstructor(PRIVATE) @Builder @EqualsAndHashCode(onlyExplicitlyIncluded) @ToString(onlyExplicitlyIncluded)` | ✅ | — |
| Embeddable IDs (6 classes) | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @EqualsAndHashCode @ToString` | ✅ ACCEPTABLE | Value objects composites — tous champs dans equals/hashCode. Correct par design. |

### 1.2 Patterns Lombok — Core Models

| Pattern | Présence |
|---------|---------|
| `@Value` | 100% (20/20) |
| `@Builder(toBuilder=true)` | 100% (20/20) |
| `@Builder.Default` sur collections | `User` (emails, identities), `Attribute` (constraintPayload), `CourseQuestionAttempt` (items), `CourseQuestionPlan` (targetKnowledgeIds) ✅ |
| `validateInvariants()` | `CourseQuestionAttempt`, `CourseQuestionPlan`, `CourseQuestionItem` ✅ |

**Bilan core models : déjà 100% conformes. Aucune modification nécessaire.**

---

### 1.3 Détection et classification des risques

#### Risque A — Boucles `toString` (NPE / StackOverflow)

| Entité | Situation | Verdict |
|--------|-----------|---------|
| `UserEntity` | Pas de `toString()` → hérite `Object.toString()` | ⚠️ Pas de boucle actuellement, mais si on ajoute `@ToString` sans `onlyExplicitlyIncluded`, les `OneToMany` déclencheraient LazyInit ou une boucle bidi |
| Toutes autres entities | `@ToString(onlyExplicitlyIncluded=true)` — seuls scalaires annotés | ✅ Sûr |

**Action requise :** lors de la migration de `UserEntity`, utiliser impérativement `@ToString(onlyExplicitlyIncluded=true)` et annoter uniquement `id` + `displayName` avec `@ToString.Include`.

#### Risque B — `equals/hashCode` impliquant des relations

| Entité | Situation | Verdict |
|--------|-----------|---------|
| `UserEntity` | Manuel, basé sur `id` uniquement | ✅ |
| `UserIdentityEntity` | Manuel, basé sur `id` uniquement | ✅ |
| `UserEmailEntity` | Manuel, basé sur `id` uniquement | ✅ |
| Toutes autres | `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` + `@EqualsAndHashCode.Include` sur `id` | ✅ |

Aucune entité n'inclut de relation dans equals/hashCode. **Pas de risque actif.**

#### Risque C — `hashCode` basé sur `id=null` + entités dans `Set`

| Entité | Situation | Verdict |
|--------|-----------|---------|
| `UserEntity.identities` → `Set<UserIdentityEntity>` | `hashCode=Objects.hash(null)=0` si `id=null`. Deux identités non-persistées ajoutées au Set avant flush → la seconde écrase la première. | ⚠️ RISQUE EXISTANT (même comportement avant/après migration Lombok) |
| Autres collections | `List<...>` → pas de problème hashCode | ✅ |

**Action :** ce risque préexiste et reste inchangé. Ajouter un commentaire sur le champ `identities` pour le documenter.

#### Risque D — Builders cassant les relations bidirectionnelles

| Entité | Situation | Verdict |
|--------|-----------|---------|
| `UserEntity` | Pas de builder actuel | ✅ — Ne pas en ajouter un |
| `PersonEntity` | `@SuperBuilder` + `@Builder.Default List<FactEntity> facts` | ✅ OK tant que les helpers `addFact/removeFact` ne sont pas bypassés |
| `CourseQuestionAttemptEntity` | `@SuperBuilder` + `@Builder.Default` + `addItem/removeItem` | ✅ OK idem |
| `ChangeRequestEntity` | `@SuperBuilder` + `@Builder.Default` + items | ✅ OK |

**Note :** `@SuperBuilder` sur ces entities peut être utilisé pour construire les collections directement sans passer par les helpers, cassant le lien inverse. À documenter dans la checklist.

#### Risque E — Collections sans `@Builder.Default`

| Entité | Situation | Verdict |
|--------|-----------|---------|
| `PersonEntity.facts`, `.photos` | `@Builder.Default` présent | ✅ |
| `ChangeRequestEntity.items` | `@Builder.Default` présent | ✅ |
| `CourseQuestionAttemptEntity.items` | `@Builder.Default` présent | ✅ |
| `UserEntity.emails`, `.identities` | Initialisées en déclaration (`= new ArrayList<>()`, `= new LinkedHashSet<>()`) — pas de builder donc pas de risque `@Builder.Default` | ✅ (pas de builder) |

---

## 2. Templates cibles

### Template A — Entity simple (scalaires uniquement)

> Référence : `UserRefreshTokenEntity` (déjà conforme)

```java
@Entity
@Table(...)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class XxxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String someScalar;

    // Relations : JAMAIS d'@EqualsAndHashCode.Include / @ToString.Include
    @ManyToOne(fetch = FetchType.LAZY)
    private OtherEntity other;
}
```

### Template B — Entity avec relations bidirectionnelles et collections

> Cible : `UserEntity` (après migration)

```java
@Entity
@Table(...)
@Getter
@Setter                           // génère setters pour scalaires
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// PAS de @AllArgsConstructor (pas de @Builder sur cette entity)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class UserEntity {

    @Id
    @GeneratedValue(...)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String displayName;

    // Champ read-only piloté par DB
    @Setter(AccessLevel.NONE)                    // empêcher setAuthUpdatedAt()
    @Column(insertable = false, updatable = false)
    private LocalDateTime authUpdatedAt;

    // Collections bidirectionnelles : setter custom conservé
    @Setter(AccessLevel.NONE)                    // setter custom ci-dessous
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEmailEntity> emails = new ArrayList<>();

    @Setter(AccessLevel.NONE)                    // setter custom ci-dessous
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserIdentityEntity> identities = new LinkedHashSet<>();

    // Constructeur de référence FK — conservation manuelle obligatoire
    public UserEntity(Long id) { this.id = id; }

    // Helpers bidirectionnels — INCHANGÉS
    public void addEmail(UserEmailEntity email) { ... }
    public void removeEmail(UserEmailEntity email) { ... }
    public void addIdentity(UserIdentityEntity identity) { ... }
    public void removeIdentity(UserIdentityEntity identity) { ... }

    // Setters custom sur collections (gèrent la bidi)
    public void setEmails(List<UserEmailEntity> emails) { ... }
    public void setIdentities(Set<UserIdentityEntity> identities) { ... }
    public void setIdentitiesFromList(List<UserIdentityEntity> identities) { ... }

    // Transients — INCHANGÉS
    @Transient public String getPrimaryEmailValue() { ... }
    @Transient public boolean hasLocalPassword() { ... }

    @PrePersist protected void onPrePersist() { ... }  // INCHANGÉ
}
```

### Template C — Core model immutable avec collections

> Déjà conforme partout. Exemple canonique :

```java
@Value
@Builder(toBuilder = true)
public class User {

    Long id;
    String displayName;

    @Builder.Default
    List<UserEmail> emails = List.of();

    @Builder.Default
    Set<UserIdentity> identities = Set.of();

    // Domain methods conservées telles quelles
    public String getPrimaryEmailValue() { ... }
    public boolean hasLocalPassword() { ... }
}
```

---

## 3. Plan de migration en étapes

### Étape 0 — Vérification préalable (avant de toucher quoi que ce soit)

```bash
# Trouver tous les usages des constructeurs directs
grep -rn "new UserEntity("         --include="*.java" .
grep -rn "new UserIdentityEntity(" --include="*.java" .
grep -rn "new UserEmailEntity("    --include="*.java" .

# Vérifier si des tests les utilisent
grep -rn "UserEntity\|UserIdentityEntity\|UserEmailEntity" \
  --include="*.java" ./persistence/src/test/
```

**Objectif :** identifier les appelants de ces constructeurs avant de restreindre leur accès.

---

### Étape 1 — `UserIdentityEntity` *(le plus simple)*

Pas de collection, pas de relation bidi côté "parent". Migration straightforward.

**Diff type :**

```diff
+import lombok.*;
+
 @Entity
 @Table(...)
+@Getter
+@Setter
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor(access = AccessLevel.PRIVATE)
+@Builder
+@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
+@ToString(onlyExplicitlyIncluded = true)
 public class UserIdentityEntity {

     @Id
     @GeneratedValue(...)
+    @EqualsAndHashCode.Include
+    @ToString.Include
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)   // PAS d'Include
     private UserEntity user;

     @Enumerated(EnumType.STRING)
+    @ToString.Include
     private AuthProvider provider;

-    // Supprimer tous les getters/setters manuels
-    public Long getId() { return id; }
-    ...

     // Conserver : @PrePersist, @PreUpdate, isLocal()
-    @Override public boolean equals(...) { ... }
-    @Override public int hashCode()      { ... }
 }
```

**Point de contrôle :** `mvn compile -pl persistence`

---

### Étape 2 — `UserEmailEntity` *(Lombok partiel → Lombok complet)*

```diff
-import lombok.experimental.SuperBuilder;
+import lombok.Builder;
 import lombok.*;

-@NoArgsConstructor
-@AllArgsConstructor
-@SuperBuilder
+@NoArgsConstructor(access = AccessLevel.PROTECTED)
+@AllArgsConstructor(access = AccessLevel.PRIVATE)
+@Builder
+@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
+@ToString(onlyExplicitlyIncluded = true)
 public class UserEmailEntity {

     @Id
     @GeneratedValue(...)
+    @EqualsAndHashCode.Include
+    @ToString.Include
     private Long id;

     @ManyToOne(fetch = FetchType.LAZY)   // PAS d'Include
     private UserEntity user;

+    @ToString.Include
     private String email;

-    @Override public boolean equals(...) { ... }
-    @Override public int hashCode()      { ... }
 }
```

**Points de contrôle :**
- `mvn compile -pl persistence`
- Vérifier que `UserEntityMapper` accède aux champs via getters (transparent avec Lombok)
- Vérifier que le `@SuperBuilder` n'était pas utilisé dans un mapper ou un test

---

### Étape 3 — `UserEntity` *(le plus complexe)*

Stratégie en 6 sous-étapes :

1. Ajouter les annotations Lombok class-level
2. Mettre `@Setter(AccessLevel.NONE)` sur `authUpdatedAt`, `emails`, `identities`
3. Annoter `id` avec `@EqualsAndHashCode.Include` + `@ToString.Include`, et `displayName` avec `@ToString.Include`
4. Supprimer tous les getters/setters manuels **sauf** `setEmails()`, `setIdentities()`, `setIdentitiesFromList()` (logique bidi custom)
5. Conserver `UserEntity(Long id)` comme constructeur manuel explicite
6. Conserver `@PrePersist`, helpers bidi, méthodes `@Transient`

**Points de contrôle :**
- `mvn compile -pl persistence`
- `mvn compile -pl service`
- Grep `new UserEntity()` → vérifier que les appelants tolèrent le constructeur PROTECTED

---

### Étape 4 — Compilation globale + smoke tests

```bash
mvn compile
mvn test -pl persistence
mvn test -pl service
```

---

## 4. Fichiers à modifier (priorisés)

| Priorité | Fichier | Risque | Raison |
|----------|---------|--------|--------|
| 🔴 1 | `persistence/.../entity/UserIdentityEntity.java` | FAIBLE | Pas de collection, pas de bidi parent, migration straightforward |
| 🔴 2 | `persistence/.../entity/UserEmailEntity.java` | FAIBLE | Aligner `@Builder` + `@EqualsAndHashCode` + `@ToString`, supprimer `@SuperBuilder` injustifié |
| 🟠 3 | `persistence/.../entity/UserEntity.java` | MOYEN | Bidi + collections + setters custom + constructeur FK à conserver manuellement |

**Core models : 0 fichier à modifier.**

**Mappers : surveiller à la compilation mais non modifiés a priori.** Si un mapper appelle `new UserEntity()` (constructeur public), il faudra adapter. Options :
- Conserver `public UserEntity()` explicitement (compromis documenté) au lieu de `@NoArgsConstructor(PROTECTED)`
- Ou migrer le mapper pour passer par un setter / repository ref

---

## 5. Cas ambigus documentés

### Business key vs id sur `UserIdentityEntity`

La paire `(provider, providerSubject)` est immuable et unique (contrainte DB `uq_ui_provider_subject`). On pourrait baser equals/hashCode dessus au lieu de `id`.

**Recommandation : conserver `id` uniquement** — cohérence avec le reste du codebase, moins de risques de NPE sur `providerSubject=null` (cas `LOCAL` possible) avant persist.

### `UserEntity(Long id)` — constructeur de référence FK

Ce pattern (créer une référence entity par id sans SELECT) doit être conservé manuellement car Lombok ne le génère pas. Alternative propre si les repos le permettent : `em.getReference(UserEntity.class, id)`. À évaluer mais hors périmètre immédiat.

### `BaseTenantScoped` sans `@EqualsAndHashCode`

Intentionnel : la superclasse ne contribue pas à l'identité. Toutes les entités enfants définissent `@EqualsAndHashCode(callSuper=false)`. **Ne pas ajouter `@EqualsAndHashCode` à `BaseTenantScoped`.**

---

## 6. Checklist de validation finale

### Entities — Safe JPA Profile

- [ ] Chaque entity a `@NoArgsConstructor(access = AccessLevel.PROTECTED)` (ou équivalent JPA)
- [ ] Chaque entity a `@Getter` et `@Setter` (ou getters/setters manuels complets)
- [ ] Chaque entity a `@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)`
- [ ] Chaque entity a `@ToString(onlyExplicitlyIncluded = true)`
- [ ] Chaque entity a exactement un champ avec `@EqualsAndHashCode.Include` → le champ `id`
- [ ] Aucune relation JPA (`@ManyToOne`, `@OneToMany`, `@ManyToMany`, `@OneToOne`) n'est annotée `@EqualsAndHashCode.Include` ou `@ToString.Include`
- [ ] Aucune entity n'utilise `@Data`
- [ ] Aucune entity n'utilise `@ToString` sans `onlyExplicitlyIncluded = true`
- [ ] Aucune entity n'utilise `@EqualsAndHashCode` sans `onlyExplicitlyIncluded = true`
- [ ] Tout `@Builder`/`@SuperBuilder` sur entity avec relations bidi est accompagné d'helpers `addX/removeX`, et les setters de collection délèguent à ces helpers
- [ ] Toute collection `@OneToMany` est initialisée non-null (`new ArrayList<>()` ou `@Builder.Default`)

### Core Models — Immutabilité + Defaults

- [ ] Chaque model a `@Value`
- [ ] Chaque model a `@Builder(toBuilder = true)`
- [ ] Toute collection (`List`, `Set`, `Map`) a `@Builder.Default` avec valeur non-null (`List.of()`, `Set.of()`)
- [ ] Aucun setter public (incompatible avec `@Value`)
- [ ] Les models avec invariants métier complexes ont `validateInvariants()` (conserver : `CourseQuestionAttempt`, `CourseQuestionPlan`, `CourseQuestionItem`)
- [ ] Les domain methods sont conservées (`User.hasLocalPassword()`, `UserRefreshToken.isExpired()`, etc.)

### Greps de vérification rapide

```bash
# @Data interdit sur entities JPA
grep -rn "@Data" --include="*.java" ./persistence/src/main/java/

# @ToString sans onlyExplicitlyIncluded
grep -rn "@ToString" --include="*.java" ./persistence/src/main/java/ \
  | grep -v "onlyExplicitlyIncluded"

# @EqualsAndHashCode sans onlyExplicitlyIncluded (hors Embeddables)
grep -rn "@EqualsAndHashCode" --include="*.java" ./persistence/src/main/java/ \
  | grep -v "onlyExplicitlyIncluded" \
  | grep -v "Id\.java"

# Relations annotées @ToString.Include ou @EqualsAndHashCode.Include (interdit)
grep -B2 "@ToString\.Include\|@EqualsAndHashCode\.Include" \
  --include="*.java" -rn ./persistence/src/main/java/ \
  | grep -E "@(ManyToOne|OneToMany|ManyToMany|OneToOne)"
```
