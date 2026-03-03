// src/main/java/com/saymyname/persistence/dao/PersonEmailDao.java
package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.people.PersonEmailEntity;
import com.saymyname.persistence.mapper.PersonEmailEntityMapper;
import com.saymyname.persistence.repository.PersonEmailRepository;

@Repository
public class PersonEmailDao {

    private final PersonEmailRepository repo;
    private final PersonEmailEntityMapper mapper;

    public PersonEmailDao(PersonEmailRepository repo, PersonEmailEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PersonEmail> listByPerson(Long personId) {
        return mapper.toModelList(repo.findByPerson(personId));
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> findById(Long id) {
        return repo.findByIdInCurrentOrg(id).map(mapper::toModel);
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> findPrimary(Long personId) {
        return repo.findPrimaryForPerson(personId).map(mapper::toModel);
    }

    @Transactional(readOnly = true)
    public boolean existsActiveByEmailIgnoreCase(String email) {
        return repo.existsActiveByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Optional<PersonEmail> findFirstActiveByEmailIgnoreCase(String email) {
        return repo.findFirstActiveByEmailIgnoreCase(email).map(mapper::toModel);
    }

    @Transactional
    public PersonEmail save(PersonEmail model) {
        Long tenantId = TenantContext.get();
        PersonEmailEntity e = mapper.toEntity(model);
        if (e.getTenantId() == null)
            e.setTenantId(tenantId);

        if (e.getPerson() == null && model.getPerson() != null && model.getPerson().getId() != null) {
            PersonEntity p = new PersonEntity();
            p.setId(model.getPerson().getId());
            p.setTenantId(tenantId);
            e.setPerson(p);
        } else if (e.getPerson() != null) {
            e.getPerson().setTenantId(tenantId);
        }

        PersonEmailEntity saved = repo.save(e);
        return mapper.toModel(saved);
    }

    @Transactional
    public PersonEmail update(PersonEmail model) {
        PersonEmailEntity current = repo.findByIdInCurrentOrg(model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Email introuvable"));

        current.setEmail(model.getEmail());
        current.setKind(model.getKind());
        current.setSourceKind(model.getSourceKind());
        current.setSourceLabel(model.getSourceLabel());
        current.setPrimary(model.isPrimary());
        current.setActive(model.isActive());
        current.setVerifiedAt(model.getVerifiedAt());
        current.setBouncedAt(model.getBouncedAt());

        PersonEmailEntity saved = repo.save(current);
        return mapper.toModel(saved);
    }

    @Transactional
    public void clearPrimary(Long personId) {
        repo.clearPrimary(personId);
    }

    @Transactional
    public void clearPrimaryExcept(Long personId, Long keepId) {
        repo.clearPrimaryExcept(personId, keepId);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteByIdInCurrentOrg(id);
    }
}
