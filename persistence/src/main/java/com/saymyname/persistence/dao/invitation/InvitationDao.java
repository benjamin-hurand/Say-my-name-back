// src/main/java/com/saymyname/persistence/dao/invitation/InvitationDao.java
package com.saymyname.persistence.dao.invitation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.mapper.invitation.InvitationEntityMapper;
import com.saymyname.persistence.repository.invitation.InvitationRepository;

@Component
public class InvitationDao {

    private final InvitationRepository repo;
    private final InvitationEntityMapper mapper;

    private static final int DEFAULT_TTL_EMAIL_DAYS = 14;
    private static final int DEFAULT_TTL_SELF_SERVICE_DAYS = 30;

    public InvitationDao(InvitationRepository repo, InvitationEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public Invitation save(Invitation model) {
        InvitationEntity e = mapper.toEntity(model);
        e.setOrganizationId(TenantContext.get());
        normalizeBeforePersist(e);
        InvitationEntity saved = repo.save(e);
        return mapper.toModel(saved);
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> findById(Long id) {
        return repo.findByIdInCurrentOrg(id).map(mapper::toModel);
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> findByTokenHash(byte[] tokenHash) {
        return repo.findByTokenHash(tokenHash).map(mapper::toModelWithPerson);
    }

    @Transactional(readOnly = true)
    public List<Invitation> listAllInOrg() {
        return repo.findAllInCurrentOrg().stream().map(mapper::toModel).toList();
    }

    @Transactional(readOnly = true)
    public List<Invitation> listByEmail(String email) {
        return repo.findAllByEmailInCurrentOrg(email).stream().map(mapper::toModel).toList();
    }

    @Transactional(readOnly = true)
    public List<Invitation> listByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty())
            return List.of();
        return repo.findAllByEmailsInCurrentOrg(emails).stream().map(mapper::toModel).toList();
    }

    @Transactional
    public Invitation update(Invitation model) {
        InvitationEntity e = mapper.toEntity(model);
        e.setOrganizationId(TenantContext.get());
        normalizeBeforePersist(e);
        InvitationEntity saved = repo.save(e);
        return mapper.toModel(saved);
    }

    @Transactional
    public void delete(Long id) {
        repo.findByIdInCurrentOrg(id).ifPresent(repo::delete);
    }

    @Transactional(readOnly = true)
    public long countInOrg() {
        return repo.countInCurrentOrg();
    }

    private void normalizeBeforePersist(InvitationEntity e) {
        if (e.getExpiresAt() == null) {
            e.setExpiresAt(computeDefaultExpiry(e));
        }

        if (e.getEmail() != null) {
            String trimmed = e.getEmail().trim();
            e.setEmail(trimmed.isEmpty() ? null : trimmed);
        }

        if (e.getMaxUses() == null || e.getMaxUses() <= 0) {
            e.setMaxUses(1);
        }

        if (e.getUsesCount() < 0) {
            e.setUsesCount(0);
        }

        // Important : maintenant qu'on stocke onboarding dedans, garde {} plutôt que
        // null
        if (e.getConstraintsJson() == null) {
            e.setConstraintsJson("{}");
        }
    }

    private LocalDateTime computeDefaultExpiry(InvitationEntity e) {
        InvitationType type = e.getType();
        int days;

        if (type == null || type == InvitationType.EMAIL) {
            days = DEFAULT_TTL_EMAIL_DAYS;
        } else if (type == InvitationType.SELF_SERVICE) {
            days = DEFAULT_TTL_SELF_SERVICE_DAYS;
        } else {
            days = DEFAULT_TTL_EMAIL_DAYS;
        }

        return LocalDateTime.now(ZoneOffset.UTC).plusDays(days);
    }
}
