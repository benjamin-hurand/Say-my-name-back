// src/main/java/com/saymyname/service/invitation/InvitationService.java
package com.saymyname.service.invitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.saymyname.core.events.invitation.InvitationCreatedEvent;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.persistence.dao.invitation.InvitationDao;
import com.saymyname.persistence.dao.invitation.InvitationUsageDao;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.repository.invitation.InvitationRepository;
import com.saymyname.service.UserOrganizationService;

@Service
public class InvitationService {

    private final InvitationDao invitationDao;
    private final InvitationUsageDao usageDao;
    private final InvitationCrypto crypto;
    private final ApplicationEventPublisher publisher;

    // Récupérer l’entité parent pour connaître l’orgId (FK) lors de l’append usage
    private final InvitationRepository invitationRepo;

    // 🔹 Service pour créer/mettre à jour la membership user_organizations
    private final UserOrganizationService userOrganizationService;

    // 🔹 Pour lire constraintsJson
    private final ObjectMapper objectMapper;

    public InvitationService(InvitationDao invitationDao,
            InvitationUsageDao usageDao,
            InvitationCrypto crypto,
            InvitationRepository invitationRepo,
            ApplicationEventPublisher publisher,
            UserOrganizationService userOrganizationService,
            ObjectMapper objectMapper) {
        this.invitationDao = invitationDao;
        this.usageDao = usageDao;
        this.crypto = crypto;
        this.invitationRepo = invitationRepo;
        this.publisher = publisher;
        this.userOrganizationService = userOrganizationService;
        this.objectMapper = objectMapper;
    }

    // ----------------- READS -----------------

    @Transactional(readOnly = true)
    public List<Invitation> list() {
        // Orga courante injectée côté DAO/Repo via OrgContext
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
        // Champs techniques
        model.setUsesCount(0);
        model.setCreatedBy(createdBy);
        model.setCreatedAt(LocalDateTime.now());

        // TTL par défaut si non fourni
        if (model.getExpiresAt() == null) {
            model.setExpiresAt(LocalDateTime.now().plusDays(14));
        }
        // Max uses par défaut
        if (model.getMaxUses() == null || model.getMaxUses() < 1) {
            model.setMaxUses(1);
        }

        // Token/pin
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token requis");
        }
        model.setTokenHash(crypto.tokenHash(rawToken));

        if (rawPin != null && !rawPin.isBlank()) {
            model.setPinHashPhc(crypto.hashPinPhc(rawPin));
        } else {
            model.setPinHashPhc(null);
        }

        // Sauvegarde : orgId injecté côté DAO via OrgContext
        Invitation saved = invitationDao.save(model);

        // Publication de l’événement
        publisher.publishEvent(new InvitationCreatedEvent(
                saved.getId(),
                rawToken,
                rawPin,
                saved.getEmail(),
                saved.getConstraintsJson()));

        return saved;
    }

    @Transactional
    public Invitation update(Invitation model) {
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
     * incrémenter le quota, ET ajouter l'utilisateur comme membre de l'orga.
     * L’orgId pour le journal d’usage est lu depuis l’entité parent
     * InvitationEntity.
     */
    @Transactional
    public Invitation acceptByToken(String rawToken,
            String rawPinNullable,
            User user,
            Person personNullable) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token requis");
        }

        byte[] th = crypto.tokenHash(rawToken);

        // 1) Charger le modèle pour validations métier (sans filtre d’orga)
        Invitation inv = invitationDao.findByTokenHash(th)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        // 🔍 Lire les contraintes d'usage (kind / requireEmailMatch /
        // requirePersonMatch)
        UseConstraints constraints = parseUseConstraints(inv.getConstraintsJson());

        // 2) Charger l’entité parent pour connaître l’orgId (FK), toujours sans filtre
        InvitationEntity parent = invitationRepo.findById(inv.getId())
                .orElseThrow(() -> new IllegalStateException("Invitation introuvable"));
        Long orgId = parent.getOrganizationId();
        if (orgId == null) {
            throw new IllegalStateException("Invitation sans organization_id");
        }

        // 3) Poser un OrgContext TEMPORAIRE basé sur l’orga de l’invitation
        Long previousOrgId = OrgContext.get();
        try {
            OrgContext.set(orgId);

            // 4) Validations métier classiques
            LocalDateTime now = LocalDateTime.now();
            if (inv.isRevoked()) {
                throw new IllegalStateException("Invitation révoquée");
            }
            if (inv.isExpired(now)) {
                throw new IllegalStateException("Invitation expirée");
            }
            if (!inv.hasRemainingUses()) {
                throw new IllegalStateException("Plus de quotas");
            }

            // Vérif PIN si présent
            if (inv.getPinHashPhc() != null) {
                if (rawPinNullable == null || rawPinNullable.isBlank()
                        || !crypto.matchesPin(rawPinNullable, inv.getPinHashPhc())) {
                    throw new IllegalArgumentException("PIN invalide");
                }
            }

            // 4bis) Contraintes d'usage basées sur constraintsJson

            // ➜ requireEmailMatch : l'utilisateur doit avoir l'email invité
            if (constraints.requireEmailMatch()) {
                String invitedEmail = inv.getEmail();
                if (invitedEmail != null && !invitedEmail.isBlank()) {
                    // On part du principe que User#getEmail() renvoie l'email primaire
                    String userEmail = user.getEmail();
                    if (userEmail == null ||
                            !invitedEmail.equalsIgnoreCase(userEmail)) {
                        throw new IllegalStateException(
                                "Cette invitation est liée à une autre adresse e-mail.");
                    }
                }
            }

            // ➜ requirePersonMatch : la Person passée doit matcher celle de l'invitation
            if (constraints.requirePersonMatch()) {
                Person invitedPerson = inv.getPerson();
                if (invitedPerson == null || invitedPerson.getId() == null) {
                    throw new IllegalStateException(
                            "Invitation mal configurée : aucune personne associée.");
                }
                if (personNullable == null || personNullable.getId() == null
                        || !invitedPerson.getId().equals(personNullable.getId())) {
                    throw new IllegalStateException(
                            "Cette invitation est liée à une autre fiche personne.");
                }
            }

            // 🔹 4ter) S'assurer que l'utilisateur est membre de l'organisation
            // Rôle pris depuis l'invitation, fallback VIEWER si null
            OrgRole invitedRole = inv.getRole() != null ? inv.getRole() : OrgRole.VIEWER;
            userOrganizationService.ensureMembership(user.getId(), orgId, invitedRole);

            // 5) Journal d’usage (append-only) – profite aussi du OrgContext
            InvitationUsage usage = new InvitationUsage.Builder()
                    .withInvitationId(inv.getId())
                    .withUser(user)
                    .withPerson(personNullable)
                    .withUsedAt(now)
                    .build();
            usageDao.appendUsage(orgId, usage, parent);

            // 6) Incrément quota + dates
            inv.setUsesCount(inv.getUsesCount() + 1);
            inv.setLastUsedAt(now);
            if (inv.getAcceptedBy() == null) {
                inv.setAcceptedBy(user);
                inv.setAcceptedAt(now);
            }

            // 7) Sauvegarde : OrgFillListener sera content, OrgContext est posé
            return invitationDao.update(inv);

        } finally {
            // 8) Restauration / nettoyage du contexte tenant
            if (previousOrgId != null) {
                OrgContext.set(previousOrgId);
            } else {
                OrgContext.clear();
            }
        }
    }

    // ----------------- Constraints JSON -----------------

    private record UseConstraints(
            String kind,
            boolean requireEmailMatch,
            boolean requirePersonMatch) {

        static final UseConstraints DEFAULT = new UseConstraints(null, false, false);
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
            return new UseConstraints(kind, requireEmailMatch, requirePersonMatch);
        } catch (Exception e) {
            // tu peux logger si besoin
            return UseConstraints.DEFAULT;
        }
    }
}
