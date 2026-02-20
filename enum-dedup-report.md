# Enum Dedup Report

Date: 2026-02-18

## Scope scannee
- `persistence/src/main/java/**`
- `domain/src/main/java/**` (absent dans ce repo)
- Scan complementaire des modules Java presents pour references (`core`, `service`, `webapp`, `security`)

## Resultat du scan (inner enums persistence)
- Trouves: 38 inner enums initialement
- Remplaces automatiquement: 24
- Non remplaces: 14

## Replaces (match 100% exact des constantes)

1. `com.saymyname.persistence.entity.EmailVerificationTokenEntity.EmailVerificationPurpose` -> `com.saymyname.core.model.enums.EmailVerificationPurpose`
- Fichiers modifies: 
  - `persistence/src/main/java/com/saymyname/persistence/entity/EmailVerificationTokenEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/auth/EmailVerificationTokenEntityMapper.java`

2. `com.saymyname.persistence.entity.TenantEntity.TenantKind` -> `com.saymyname.core.model.enums.tenant.TenantKind`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/TenantEntity.java`

3. `com.saymyname.persistence.entity.UserEntity.SrsAlgorithm` -> `com.saymyname.core.model.enums.SrsAlgorithm`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/UserEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/UserEntityMapper.java`

4. `com.saymyname.persistence.entity.UserIdentityEntity.IdentityProvider` -> `com.saymyname.core.model.enums.AuthProvider`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/UserIdentityEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/UserIdentityEntityMapper.java`

5. `com.saymyname.persistence.entity.organization.ChangeRequestEntity.ChangeRequestStatus` -> `com.saymyname.core.model.enums.ChangeRequestStatus`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/ChangeRequestEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/ChangeRequestEntityMapper.java`

6. `com.saymyname.persistence.entity.organization.ChangeRequestItemEntity.ChangeAction` -> `com.saymyname.core.model.enums.ChangeAction`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/ChangeRequestItemEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/ChangeRequestItemEntityMapper.java`

7. `com.saymyname.persistence.entity.organization.ChangeRequestItemEntity.ResolutionStatus` -> `com.saymyname.core.model.enums.ChangeRequestItemStatus`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/ChangeRequestItemEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/ChangeRequestItemEntityMapper.java`

8. `com.saymyname.persistence.entity.organization.attribute.AttributeEntity.EditPolicy` -> `com.saymyname.core.model.enums.EditPolicy`

9. `com.saymyname.persistence.entity.organization.attribute.AttributeEntity.CasingStrategy` -> `com.saymyname.core.model.enums.CasingStrategy`

10. `com.saymyname.persistence.entity.organization.attribute.AttributeEntity.ConstraintKind` -> `com.saymyname.core.model.enums.ConstraintKind`
- Fichiers modifies (8-10):
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/attribute/AttributeEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/AttributeEntityMapper.java`

11. `com.saymyname.persistence.entity.organization.course.CourseEntity.CourseTargetScope` -> `com.saymyname.core.model.enums.CourseTargetScope`

12. `com.saymyname.persistence.entity.organization.course.CourseEntity.CourseStatus` -> `com.saymyname.core.model.enums.CourseStatus`

13. `com.saymyname.persistence.entity.organization.course.CourseEntity.PopulationScope` -> `com.saymyname.core.model.enums.PopulationScope`
- Fichiers modifies (11-13):
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/course/CourseEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/course/CourseEntityMapper.java`

14. `com.saymyname.persistence.entity.organization.OrganizationEntity.OrgType` -> `com.saymyname.core.model.enums.OrgType`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/OrganizationEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/organization/OrganizationEntityMapper.java`

15. `com.saymyname.persistence.entity.organization.invitation.InvitationEntity.InvitationType` -> `com.saymyname.core.model.enums.InvitationType`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/invitation/InvitationEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/invitation/InvitationEntityMapper.java`

16. `com.saymyname.persistence.entity.organization.PhotoEntity.PhotoStatus` -> `com.saymyname.core.model.enums.PhotoStatus`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/PhotoEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/PhotoEntityMapper.java`

17. `com.saymyname.persistence.entity.organization.PhotoReportEntity.PhotoReportReasonType` -> `com.saymyname.core.model.enums.PhotoReportReason`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/PhotoReportEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/PhotoReportEntityMapper.java`

18. `com.saymyname.persistence.entity.organization.people.PersonEmailEntity.EmailKind` -> `com.saymyname.core.model.enums.EmailKind`

19. `com.saymyname.persistence.entity.organization.people.PersonEmailEntity.SourceKind` -> `com.saymyname.core.model.enums.EmailSourceKind`
- Fichiers modifies (18-19):
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/people/PersonEmailEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/PersonEmailEntityMapper.java`

20. `com.saymyname.persistence.entity.organization.course.KnowledgeEntity.KnowledgeStatus` -> `com.saymyname.core.model.enums.KnowledgeStatus`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/course/KnowledgeEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/course/KnowledgeEntityMapper.java`

21. `com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity.PoolType` -> `com.saymyname.core.model.enums.PoolType`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/course/CourseQuestionAttemptEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionAttemptEntityMapper.java`

22. `com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity.ItemRole` -> `com.saymyname.core.model.enums.course.QuizQuestionItemRole`
- Fichiers modifies:
  - `persistence/src/main/java/com/saymyname/persistence/entity/organization/course/CourseQuestionItemEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionItemEntityMapper.java`

23. `com.saymyname.persistence.entity.workspace.WorkspaceMemberEntity.WorkspaceRole` -> `com.saymyname.core.model.enums.workspace.WorkspaceRole`

24. `com.saymyname.persistence.entity.workspace.WorkspaceMemberEntity.WorkspaceMemberStatus` -> `com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus`
- Fichiers modifies (23-24):
  - `persistence/src/main/java/com/saymyname/persistence/entity/workspace/WorkspaceMemberEntity.java`
  - `persistence/src/main/java/com/saymyname/persistence/mapper/workspace/WorkspaceMemberEntityMapper.java`

## Non remplaces (manuel)

1. `com.saymyname.persistence.entity.organization.ScopeKind`
- Raison: ambiguite (match exact avec `com.saymyname.core.model.enums.ScopeKind` et `com.saymyname.core.model.enums.tenant.ScopeKind`).

2. `com.saymyname.persistence.entity.organization.PhotoAssignmentEntity.ScopeKind`
- Raison: ambiguite (meme cas que ci-dessus).

3. `com.saymyname.persistence.entity.organization.UserOrganizationEntity.PersonLinkStatus`
- Raison: ambiguite (match exact avec `com.saymyname.core.model.enums.PersonLinkStatus` et `com.saymyname.core.model.enums.workspace.PersonLinkStatus`).

4. `com.saymyname.persistence.entity.workspace.WorkspaceMemberEntity.PersonLinkStatus`
- Raison: ambiguite (meme cas).

5. `com.saymyname.persistence.entity.organization.UserOrganizationEntity.MemberRole`
- Raison: plusieurs enums core avec meme set (`OrgRole`, `workspace.WorkspaceRole`).

6. `com.saymyname.persistence.entity.organization.invitation.InvitationEntity.InvitationRole`
- Raison: plusieurs enums core avec meme set (`OrgRole`, `workspace.WorkspaceRole`).

7. `com.saymyname.persistence.entity.organization.UserOrganizationEntity.MemberStatus`
- Raison: plusieurs enums core avec meme set (`MembershipStatus`, `workspace.WorkspaceMemberStatus`).

8. `com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity.QuestionFormat`
- Raison: pas d'enum equivalent exact sous `core/.../enums`.

9. `com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity.PlannedFormat`
- Raison: pas d'enum equivalent exact sous `core/.../enums`.

10. `com.saymyname.persistence.entity.organization.course.CourseRecentStatsEntity.LastFormat`
- Raison: pas d'enum equivalent exact sous `core/.../enums`.

11. `com.saymyname.persistence.entity.workspace.ImportBatchEntity.SourceKind`
- Raison: pas d'enum core equivalent.

12. `com.saymyname.persistence.entity.workspace.ImportBatchEntity.ImportBatchStatus`
- Raison: pas d'enum core equivalent.

13. `com.saymyname.persistence.entity.workspace.ImportMappingEntity.TransformKind`
- Raison: pas d'enum core equivalent.

14. `com.saymyname.persistence.entity.workspace.ImportRowEntity.ImportRowStatus`
- Raison: pas d'enum core equivalent.

## Validation build/tests
- Commande lancee: `mvn -q -pl persistence -am clean compile`
- Resultat: echec **non lie a ce refactoring** sur `persistence/src/main/java/com/saymyname/persistence/dao/course/CourseDao.java` (reference manquante `GameModeRepository`).
- Aucun echec de compilation additionnel specifique aux enums remplaces n'a ete isole avant ce blocage.
- Commande lancee: `mvn -q -pl persistence -am test`
- Resultat: echec dans `core` sur `AttributeValueValidatorTest` (3 assertions en echec), avant la phase de test `persistence`.
