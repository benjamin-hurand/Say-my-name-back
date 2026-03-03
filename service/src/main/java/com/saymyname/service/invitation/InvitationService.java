// src/main/java/com/saymyname/service/invitation/InvitationService.java
package com.saymyname.service.invitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.saymyname.core.events.invitation.InvitationCreatedEvent;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.invitation.InvitationDao;
import com.saymyname.persistence.dao.invitation.InvitationUsageDao;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.repository.invitation.InvitationRepository;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.service.UserService;

@Service
public class InvitationService {

    private final InvitationDao invitationDao;
    private final InvitationUsageDao usageDao;
    private final InvitationCrypto crypto;
    private final ApplicationEventPublisher publisher;

    // Récupérer l’entité parent pour connaître l’tenantId (FK) lors de l’append
    // usage
    private final InvitationRepository invitationRepo;

    // Service membership
    private final UserOrganizationService userOrganizationService;

    // ✅ Service user pour vérifier email (verifiedAt != null)
    private final UserService userService;

    // Pour lire constraintsJson
    private final ObjectMapper objectMapper;

    public InvitationService(
            InvitationDao invitationDao,
            InvitationUsageDao usageDao,
            InvitationCrypto crypto,
            InvitationRepository invitationRepo,
            ApplicationEventPublisher publisher,
            UserOrganizationService userOrganizationService,
            UserService userService,
            ObjectMapper objectMapper) {
        this.invitationDao = invitationDao;
        this.usageDao = usageDao;
        this.crypto = crypto;
        this.invitationRepo = invitationRepo;
        this.publisher = publisher;
        this.userOrganizationService = userOrganizationService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    // ----------------- READS -----------------

    @Transactional(readOnly = true)
    public List<Invitation> list() {
        return invitationDao.listAllInOrg();
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> get(Long id) {
        return invitationDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Invitation previewByToken(String rawToken) {
        byte[] th = crypto.tokenHash(rawToken);
        return invitationDao.findByTokenHash(th)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));
    }

    // ----------------- COMMANDS -----------------

    @Transactional
    public Invitation create(Invitation model, User createdBy, String rawToken, String rawPin) {
        LocalDateTime now = LocalDateTime.now();
        model.setUsesCount(0);
        model.setCreatedBy(createdBy);
        model.setCreatedAt(now);

        UseConstraints c = parseUseConstraints(model.getConstraintsJson());
        boolean isGroup = "GROUP".equalsIgnoreCase(c.kind());

        // ✅ Appliquer expiryMode si expiresAt non fourni explicitement
        if (model.getExpiresAt() == null) {
            String expiryMode = c.expiryMode(); // on va l’ajouter dans UseConstraints
            LocalDateTime computed = computeExpiresAtFromExpiryMode(expiryMode, now);
            model.setExpiresAt(computed);
        }

        // ✅ Defaults selon type
        if (model.getExpiresAt() == null) {
            model.setExpiresAt(isGroup ? now.plusDays(30) : now.plusDays(14));
        }

        // group => illimité (null), nominatif => 1 (preservation de l'existant)
        if (model.getMaxUses() == null) {
            model.setMaxUses(isGroup ? null : 1);
        } else if (model.getMaxUses() != null && model.getMaxUses() < 1) {
            model.setMaxUses(isGroup ? null : 1);
        }

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token requis");
        }
        model.setTokenHash(crypto.tokenHash(rawToken));

        if (rawPin != null && !rawPin.isBlank()) {
            model.setPinHashPhc(crypto.hashPinPhc(rawPin));
        } else {
            model.setPinHashPhc(null);
        }

        // Optionnel : s'assurer qu'onboarding a des defaults si constraintsJson est
        // vide/absent
        model.setConstraintsJson(ensureConstraintsDefaults(model.getConstraintsJson(), model));

        Invitation saved = invitationDao.save(model);
        if (saved.getType() == InvitationType.EMAIL && saved.getEmail() != null && !saved.getEmail().isBlank()) {
            publisher.publishEvent(new InvitationCreatedEvent(
                    saved.getId(),
                    rawToken,
                    rawPin,
                    saved.getEmail(),
                    saved.getConstraintsJson()));
        }

        return saved;
    }

    @Transactional
    public Invitation update(Invitation model) {
        model.setConstraintsJson(ensureConstraintsDefaults(model.getConstraintsJson(), model));
        return invitationDao.update(model);
    }

    @Transactional
    public void delete(Long id) {
        invitationDao.delete(id);
    }

    @Transactional
    public Invitation revoke(Long id, User by) {
        Invitation inv = invitationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation introuvable"));
        if (inv.getRevokedAt() == null) {
            inv.setRevokedAt(LocalDateTime.now());
            inv = invitationDao.update(inv);
        }
        return inv;
    }

    /**
     * Consommer une invitation (token + PIN éventuel), journaliser l’usage,
     * incrémenter le quota, ET ajouter l'utilisateur comme membre de l'orga
     * avec le snapshot onboarding.
     */
    @Transactional
    public Invitation acceptByToken(
            String rawToken,
            String rawPinNullable,
            User user,
            Person personNullable) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token requis");
        }

        byte[] th = crypto.tokenHash(rawToken);

        // 1) Charger le modèle (sans filtre d’orga)
        Invitation inv = invitationDao.findByTokenHash(th)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        // 2) Lire constraintsJson (usage + onboarding)
        UseConstraints constraints = parseUseConstraints(inv.getConstraintsJson());

        // 3) Charger l’entité parent pour tenantId (FK)
        InvitationEntity parent = invitationRepo.findById(inv.getId())
                .orElseThrow(() -> new IllegalStateException("Invitation introuvable"));

        Long tenantId = parent.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Invitation sans tenant_id");
        }

        // 4) Poser un TenantContext temporaire basé sur l’orga de l’invitation
        Long previousOrgId = TenantContext.get();
        try {
            TenantContext.set(tenantId);

            LocalDateTime now = LocalDateTime.now();

            // 5) Validations
            if (inv.isRevoked())
                throw new IllegalStateException("Invitation révoquée");
            if (inv.isExpired(now))
                throw new IllegalStateException("Invitation expirée");
            if (!inv.hasRemainingUses())
                throw new IllegalStateException("Plus de quotas");

            // PIN
            if (inv.getPinHashPhc() != null) {
                if (rawPinNullable == null || rawPinNullable.isBlank()
                        || !crypto.matchesPin(rawPinNullable, inv.getPinHashPhc())) {
                    throw new IllegalArgumentException("PIN invalide");
                }
            }

            // ✅ requireEmailMatch : on accepte si l'utilisateur A DÉJÀ vérifié l'email
            // invité
            if (constraints.requireEmailMatch()) {
                String invitedEmail = normalizeEmail(inv.getEmail());
                if (invitedEmail != null) {
                    Long userId = user != null ? user.getId() : null;
                    if (userId == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
                    }

                    boolean ok = userService.hasVerifiedEmail(userId, invitedEmail);
                    if (!ok) {
                        // Ici, on déclenche le flow UI: proposer d'ajouter + vérifier l'email invité,
                        // puis retenter acceptByToken.
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "EMAIL_VERIFICATION_REQUIRED:" + invitedEmail);
                    }
                }
            }

            // requirePersonMatch
            Long effectivePersonId;
            if (constraints.requirePersonMatch()) {
                Person invitedPerson = inv.getPerson();
                if (invitedPerson == null || invitedPerson.getId() == null) {
                    throw new IllegalStateException("Invitation mal configurée : aucune personne associée.");
                }
                if (personNullable == null || personNullable.getId() == null
                        || !invitedPerson.getId().equals(personNullable.getId())) {
                    throw new IllegalStateException("Cette invitation est liée à une autre fiche personne.");
                }
                effectivePersonId = invitedPerson.getId();
            } else {
                effectivePersonId = (personNullable != null ? personNullable.getId() : null);
            }

            // 6) Construire le snapshot membership
            OrgRole invitedRole = inv.getRole() != null ? inv.getRole() : OrgRole.VIEWER;
            OnboardingPolicy policy = constraints.onboarding();

            // Invitations nominatives (person imposée) : aucun onboarding à faire
            if (inv.getPerson() != null && inv.getPerson().getId() != null) {
                policy = OnboardingPolicy.NOMINATIVE_LOCKED;
                effectivePersonId = inv.getPerson().getId();
            }

            PersonLinkStatus linkStatus = computePersonLinkStatus(effectivePersonId, policy);

            // 7) Assurer membership complète
            userOrganizationService.ensureMembershipFromInvitation(
                    user.getId(),
                    tenantId,
                    invitedRole,
                    MembershipStatus.ACTIVE,
                    effectivePersonId,
                    linkStatus,
                    policy.canPickPerson(),
                    policy.canCreatePerson(),
                    policy.pickRequiresApproval(),
                    policy.createRequiresApproval());

            // 8) Journal d’usage (append-only)
            InvitationUsage usage = new InvitationUsage.Builder()
                    .withInvitationId(inv.getId())
                    .withUser(user)
                    .withPerson(personNullable)
                    .withUsedAt(now)
                    .build();
            usageDao.appendUsage(tenantId, usage, parent);

            // 9) Incrément quota + dates invitation
            inv.setUsesCount(inv.getUsesCount() + 1);
            inv.setLastUsedAt(now);
            if (inv.getAcceptedBy() == null) {
                inv.setAcceptedBy(user);
                inv.setAcceptedAt(now);
            }

            return invitationDao.update(inv);

        } finally {
            if (previousOrgId != null)
                TenantContext.set(previousOrgId);
            else
                TenantContext.clear();
        }
    }

    // ----------------- Constraints JSON -----------------

    /**
     * OnboardingPolicy :
     * - defaults (safe)
     * - et un cas "NOMINATIVE_LOCKED" si person imposée.
     */
    private record OnboardingPolicy(
            boolean canPickPerson,
            boolean canCreatePerson,
            boolean pickRequiresApproval,
            boolean createRequiresApproval) {

        static final OnboardingPolicy DEFAULT = new OnboardingPolicy(false, true, true, false);
        static final OnboardingPolicy NOMINATIVE_LOCKED = new OnboardingPolicy(false, false, false, false);
    }

    private record UseConstraints(
            String kind,
            boolean requireEmailMatch,
            boolean requirePersonMatch,
            String expiryMode,
            OnboardingPolicy onboarding) {

        static final UseConstraints DEFAULT = new UseConstraints(null, false, false, null, OnboardingPolicy.DEFAULT);
    }

    private UseConstraints parseUseConstraints(String json) {
        if (json == null || json.isBlank()) {
            return UseConstraints.DEFAULT;
        }
        try {
            JsonNode root = objectMapper.readTree(json);

            String kind = root.path("kind").asText(null);
            boolean requireEmailMatch = root.path("requireEmailMatch").asBoolean(false);
            boolean requirePersonMatch = root.path("requirePersonMatch").asBoolean(false);
            String expiryMode = root.path("expiryMode").asText(null);

            JsonNode onboardingNode = root.path("onboarding");
            OnboardingPolicy onboarding;
            if (onboardingNode.isMissingNode() || onboardingNode.isNull()) {
                onboarding = OnboardingPolicy.DEFAULT;
            } else {
                onboarding = new OnboardingPolicy(
                        onboardingNode.path("canPickPerson").asBoolean(OnboardingPolicy.DEFAULT.canPickPerson()),
                        onboardingNode.path("canCreatePerson").asBoolean(OnboardingPolicy.DEFAULT.canCreatePerson()),
                        onboardingNode.path("pickRequiresApproval")
                                .asBoolean(OnboardingPolicy.DEFAULT.pickRequiresApproval()),
                        onboardingNode.path("createRequiresApproval")
                                .asBoolean(OnboardingPolicy.DEFAULT.createRequiresApproval()));
            }

            return new UseConstraints(kind, requireEmailMatch, requirePersonMatch, expiryMode, onboarding);

        } catch (Exception e) {
            return UseConstraints.DEFAULT;
        }
    }

    private String ensureConstraintsDefaults(String json, Invitation inv) {
        try {
            JsonNode root = (json == null || json.isBlank())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(json);

            var obj = root.isObject()
                    ? (com.fasterxml.jackson.databind.node.ObjectNode) root
                    : objectMapper.createObjectNode();

            if (!obj.has("onboarding") || obj.get("onboarding").isNull()) {
                OnboardingPolicy policy = OnboardingPolicy.DEFAULT;

                // Si invitation nominative (person_id), on verrouille
                if (inv != null && inv.getPerson() != null && inv.getPerson().getId() != null) {
                    policy = OnboardingPolicy.NOMINATIVE_LOCKED;
                }

                var onboarding = objectMapper.createObjectNode();
                onboarding.put("canPickPerson", policy.canPickPerson());
                onboarding.put("canCreatePerson", policy.canCreatePerson());
                onboarding.put("pickRequiresApproval", policy.pickRequiresApproval());
                onboarding.put("createRequiresApproval", policy.createRequiresApproval());

                obj.set("onboarding", onboarding);
            }

            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"onboarding\":{\"canPickPerson\":false,\"canCreatePerson\":true,\"pickRequiresApproval\":true,\"createRequiresApproval\":false}}";
        }
    }

    private LocalDateTime computeExpiresAtFromExpiryMode(@Nullable String expiryMode, LocalDateTime now) {
        if (expiryMode == null || expiryMode.isBlank())
            return null;

        return switch (expiryMode) {
            case "HOURS_24" -> now.plusHours(24);
            case "DAYS_7" -> now.plusDays(7);
            case "DAYS_30" -> now.plusDays(30);
            case "DAYS_90" -> now.plusDays(90);
            case "NEVER" -> null; // si vous gardez NEVER
            default -> null; // fallback safe
        };
    }

    /**
     * ✅ Ton enum PersonLinkStatus = NONE/PENDING/APPROVED/REJECTED
     * - personId null -> NONE
     * - approval requis -> PENDING
     * - sinon -> APPROVED
     */
    private PersonLinkStatus computePersonLinkStatus(Long personId, OnboardingPolicy policy) {
        if (personId == null) {
            return PersonLinkStatus.NONE;
        }
        boolean needsApproval = policy.pickRequiresApproval() || policy.createRequiresApproval();
        return needsApproval ? PersonLinkStatus.PENDING : PersonLinkStatus.APPROVED;
    }

    private static String normalizeEmail(String email) {
        if (email == null)
            return null;
        String t = email.trim();
        return t.isBlank() ? null : t;
    }
}
