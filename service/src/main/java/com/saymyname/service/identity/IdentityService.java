package com.saymyname.service.identity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.identity.IdentityResolver;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.attribute.AttributeMetaCache;

@Service
public class IdentityService {

    private final AttributeMetaCache attributeMetaCache;
    private final FactDao factDao;
    private final IdentityResolver identityResolver;
    private final PersonDao personDao;

    public IdentityService(
            AttributeMetaCache attributeMetaCache,
            FactDao factDao,
            IdentityResolver identityResolver,
            PersonDao personDao) {
        this.attributeMetaCache = attributeMetaCache;
        this.factDao = factDao;
        this.identityResolver = identityResolver;
        this.personDao = personDao;
    }

    @Transactional
    public void synchronizeAllCurrentTenant(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now est requis");
        }
        for (Long personId : personDao.findAllIds()) {
            synchronize(personId, now);
        }
    }

    @Transactional
    public void synchronize(Long personId, LocalDateTime now) {
        if (personId == null || now == null) {
            throw new IllegalArgumentException("personId et now sont requis");
        }

        // Also protects direct/retry calls. FactService acquires the same reentrant
        // row lock before mutating a source, i.e. before the transaction snapshot.
        factDao.lockPersonForUpdate(personId);

        Long identityAttributeId = attributeMetaCache.getIdentityAttributeId();
        if (identityAttributeId == null) {
            throw new IllegalStateException("L'attribut système IDENTITY est introuvable pour le tenant courant");
        }

        List<String> sourceValues = new ArrayList<>();
        for (Long sourceAttributeId : attributeMetaCache.getIdentitySourceAttributeIds()) {
            factDao.findActiveAtByPersonAndAttribute(personId, sourceAttributeId, now).stream()
                    .map(Fact::getValue)
                    .forEach(sourceValues::add);
        }

        String identity = identityResolver.compose(sourceValues);
        List<Fact> current = factDao.findActiveAtByPersonAndAttribute(personId, identityAttributeId, now);

        if (!identity.isEmpty()
                && current.size() == 1
                && Objects.equals(identity, current.getFirst().getValue())) {
            return;
        }

        List<Long> currentIds = current.stream()
                .map(Fact::getId)
                .filter(Objects::nonNull)
                .toList();
        factDao.softCloseActiveByIdsAndPersonId(personId, currentIds, now);

        if (!identity.isEmpty()) {
            factDao.createAllForPersonAt(personId, identityAttributeId, List.of(identity), now);
        }
    }
}
